package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
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
    try {
      val message: ReturnMessage = objectMapper.readValue(rawMessage)

      when (message.status) {
        ReturnStatus.REJECTED -> returnsEventProcessor.onRejected(message)
      }
    } catch (e: Exception) {
      log.error("Failed to process returns event: ${e.message}")
      throw e
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
