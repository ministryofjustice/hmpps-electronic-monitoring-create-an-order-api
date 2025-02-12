package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.HearingEvent
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
  data class S3Message(
    val s3BucketName: String,
    val s3Key: String,
  )
  private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")

  @SqsListener("courthearingeventqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onDomainEvent(rawMessage: String) {
    val startTimeInMs = System.currentTimeMillis()
    val startDateTime = ZonedDateTime.now(ZoneId.of("GMT"))
    try {
      val eventMessage: HmppsSqsEventMessage = objectMapper.readValue(rawMessage)
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
    } catch (e: Exception) {
      deadLetterQueueService.sentEvent(rawMessage, "Malformed event received. Could not parse JSON")
      // TODO Handle messages in dead letter queue
      val error = e.message ?: ""
      eventService.recordEvent(
        "Common_Platform_Exception",
        mapOf("Start Date And Time" to startDateTime.format(formatter)),
        System.currentTimeMillis() - startTimeInMs,
      )
    }
  }
}
