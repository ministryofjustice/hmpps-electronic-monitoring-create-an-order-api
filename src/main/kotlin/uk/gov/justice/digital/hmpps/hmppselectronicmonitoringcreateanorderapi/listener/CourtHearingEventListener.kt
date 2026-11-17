package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.HearingEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.CorrelationIdContext
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.HmppsSqsEventMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.S3Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.DeadLetterQueueService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.HearingEventHandler
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
@ConditionalOnExpression("\${toggle.common-platform.processing.enabled:false}")
class CourtHearingEventListener(
  private val eventHandler: HearingEventHandler,
  private val deadLetterQueueService: DeadLetterQueueService,
  private val objectMapper: ObjectMapper,
  private val eventService: EventService,
  private val s3Service: S3Service,
) {
  data class S3Message(val s3BucketName: String, val s3Key: String)

  private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")

  /*
   * wrapping this in MDC so we can store this metadata on the thread and get telemetry out.
   * MAINTAINER: MDC won't work properly if we change its call sites to be async
   *         this fits because of our fms client call block convention
   */
  @SqsListener("courthearingeventqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onDomainEvent(rawMessage: String) {
    val startTimeInMs = System.currentTimeMillis()
    val startDateTime = ZonedDateTime.now(ZoneId.of("GMT"))
    var correlationId = CorrelationIdContext.newId()
    try {
      val eventMessage: HmppsSqsEventMessage = objectMapper.readValue(rawMessage)
      correlationId = CorrelationIdContext.validOrNew(eventMessage.messageId)
      CorrelationIdContext.withCorrelationId(correlationId) {
        val courtHearing: HearingEvent
        if (eventMessage.messageAttributes.eventType.value == "commonplatform.large.case.received") {
          val messageArray = objectMapper.readValue(eventMessage.message, ArrayList::class.java)
          val s3MessageBody = objectMapper.writeValueAsString(messageArray[1])
          val s3Message = objectMapper.readValue(s3MessageBody, S3Message::class.java)
          courtHearing = objectMapper.readValue(s3Service.getObject(s3Message.s3Key))
        } else {
          courtHearing = objectMapper.readValue(eventMessage.message)
        }

        // Process message if contains EM details, else ignore
        if (courtHearing.hearing.isHearingContainsEM()) {
          val handlingErrors = eventHandler.handleHearingEvent(courtHearing)
          if (handlingErrors.any()) {
            deadLetterQueueService.sentEvent(rawMessage, handlingErrors.joinToString(","))
          }
        } else {
          val containsEmLabel = rawMessage.contains("Notification of electronic monitoring order")
          eventService.recordEvent(
            "Common_Platform_Ignored_Request",
            mapOf(
              "Contain notification of electronic monitoring order label" to containsEmLabel.toString(),
              "Start Date And Time" to startDateTime.format(formatter),
            ),
            System.currentTimeMillis() - startTimeInMs,
          )
        }
      }
    } catch (_: Exception) {
      // remove c-id reference from thread on exception
      CorrelationIdContext.withCorrelationId(correlationId) {
        deadLetterQueueService.sentEvent(rawMessage, "Malformed event received. Could not parse JSON")
        // TODO Handle messages in dead letter queue
        eventService.recordEvent(
          "Common_Platform_Exception",
          mapOf("Start Date And Time" to startDateTime.format(formatter)),
          System.currentTimeMillis() - startTimeInMs,
        )
      }
    }
  }
}
