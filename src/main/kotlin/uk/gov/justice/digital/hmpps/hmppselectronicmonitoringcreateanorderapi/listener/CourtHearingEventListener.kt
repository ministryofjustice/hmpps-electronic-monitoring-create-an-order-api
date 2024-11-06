package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.HearingEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.HmppsSqsEventMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.DeadLetterQueueService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.HearingEventHandler

@Service
@ConditionalOnExpression("\${toggle.cp-integration.enabled:false}")
class CourtHearingEventListener(
  private val eventHandler: HearingEventHandler,
  private val deadLetterQueueService: DeadLetterQueueService,
  private val objectMapper: ObjectMapper,
) {

  @SqsListener("courthearingeventqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onDomainEvent(rawMessage: String) {
    try {
      val eventMessage: HmppsSqsEventMessage = objectMapper.readValue(rawMessage)
      val courtHearing: HearingEvent = objectMapper.readValue(eventMessage.message)

      // Process message if contains EM details, else ignore
      if (courtHearing.hearing.isHearingContainsEM()) {
        val result = eventHandler.handleHearingEvent(courtHearing)
        if (result.any()) {
          deadLetterQueueService.sentEvent(rawMessage, result.joinToString(","))
        }
      }
    } catch (e: Exception) {
      deadLetterQueueService.sentEvent(rawMessage, "Malformed event received. Could not parse JSON")
      // TODO Handle messages in dead letter queue
    }
  }
}
