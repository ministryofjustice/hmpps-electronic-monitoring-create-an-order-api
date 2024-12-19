package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmitFmsOrderResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.SubmitFmsOrderResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsDummySubmissionStrategy
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsOrderSubmissionStrategy
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsSubmissionStrategy
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsVariationSubmissionStrategy

@Service
@Configuration
class FmsService(
  val fmsClient: FmsClient,
  val objectMapper: ObjectMapper,
  val submitFmsOrderResultRepository: SubmitFmsOrderResultRepository,
  @Value("\${toggle.fms-integration.enabled:false}") val fmsIntegrationEnabled: Boolean,
) {
  private fun getSubmissionStrategy(order: Order, orderSource: FmsOrderSource): FmsSubmissionStrategy {
    if (!fmsIntegrationEnabled || orderSource === FmsOrderSource.COMMON_PLATFORM) {
      return FmsDummySubmissionStrategy(this.objectMapper)
    }

    if (order.type === OrderType.VARIATION) {
      return FmsVariationSubmissionStrategy(this.objectMapper, this.fmsClient)
    }

    return FmsOrderSubmissionStrategy(this.objectMapper, this.fmsClient)
  }

  fun submitOrder(order: Order, orderSource: FmsOrderSource): SubmitFmsOrderResult {
    val strategy = this.getSubmissionStrategy(order, orderSource)
    val submissionResult = strategy.submitOrder(order, orderSource)

    submitFmsOrderResultRepository.save(submissionResult)

    return submissionResult
  }
}
