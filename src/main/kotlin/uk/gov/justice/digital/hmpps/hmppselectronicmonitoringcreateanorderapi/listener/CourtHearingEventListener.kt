package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.HearingEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.HmppsSqsEventMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.DeadLetterQueueService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.HearingEventHandler

@Service
@ConditionalOnExpression("\${toggle.cp-integration.enabled:false}")
class CourtHearingEventListener(
  private val eventHandler: HearingEventHandler,
  private val deadLetterQueueService: DeadLetterQueueService,
  private val objectMapper: ObjectMapper,
  private val eventService: EventService,
) {

  @SqsListener("courthearingeventqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onDomainEvent(rawMessage: String) {
    val startTimeInMs = System.currentTimeMillis()
    try {
      val eventMessage: HmppsSqsEventMessage = objectMapper.readValue(rawMessage)
      val courtHearing: HearingEvent = objectMapper.readValue(eventMessage.message)

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
          mapOf("Contain notification of electronic monitoring order label" to containsEmLabel.toString()),
          System.currentTimeMillis() - startTimeInMs,
        )
      }
    } catch (e: Exception) {
      deadLetterQueueService.sentEvent(rawMessage, "Malformed event received. Could not parse JSON")
      // TODO Handle messages in dead letter queue
      val error = e.message ?: ""
      eventService.recordEvent(
        "Common_Platform_Exception",
        mapOf("exception" to error, "stacktrace" to e.stackTraceToString()),
        System.currentTimeMillis() - startTimeInMs,
      )
    }
  }
}
