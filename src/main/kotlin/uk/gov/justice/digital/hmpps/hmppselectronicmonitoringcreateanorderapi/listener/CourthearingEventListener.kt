package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Hearing
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.HmppsSqsEventMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.DeadLetterQueueService

@Service
class CourthearingEventListener(
  private val deadLetterQueueService: DeadLetterQueueService,
  private val objectMapper: ObjectMapper,
) {

  @SqsListener("courthearingeventqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onDomainEvent(rawMessage: String) {
    try {
      val eventMessage: HmppsSqsEventMessage = objectMapper.readValue(rawMessage)
      val courtHearing: Hearing = objectMapper.readValue(eventMessage.message)

      // TODO process message
      // Process message if contains EM details, else ignore
      if (courtHearing.isHearingContainsEM()) {
      }
    } catch (e: Exception) {
      deadLetterQueueService.sentEvent(rawMessage, "Malformed event received. Could not parse JSON")
    }
  }
}
