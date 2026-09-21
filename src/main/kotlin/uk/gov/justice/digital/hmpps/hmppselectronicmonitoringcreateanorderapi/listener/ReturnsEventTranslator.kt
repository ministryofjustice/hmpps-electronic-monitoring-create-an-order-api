package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnStatus

@Component
class ReturnsEventTranslator(
  private val returnsEventProcessor: ReturnsEventProcessor,
  private val objectMapper: ObjectMapper,
) {
  @SqsListener("returnseventqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun processEvent(rawMessage: String) {
    val message: ReturnMessage = objectMapper.readValue(rawMessage)

    when (message.status) {
      ReturnStatus.REJECTED -> returnsEventProcessor.onRejected(message)
    }
  }
}
