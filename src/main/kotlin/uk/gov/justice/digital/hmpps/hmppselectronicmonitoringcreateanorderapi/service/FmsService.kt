package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsDummySubmissionStrategy
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsOrderSubmissionStrategy
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsSubmissionStrategy
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsVariationSubmissionStrategy

@Service
@Configuration
class FmsService(
  val fmsClient: FmsClient,
  val documentApiClient: DocumentApiClient,
  val objectMapper: ObjectMapper,
  val repo: FmsSubmissionResultRepository,
  @Value("\${toggle.cemo.fms-integration.enabled:false}") val cemoFmsIntegrationEnabled: Boolean,
  @Value("\${toggle.common-platform.fms-integration.enabled:false}") val cpFmsIntegrationEnabled: Boolean,
) {
  private fun getSubmissionStrategy(order: Order, orderSource: FmsOrderSource): FmsSubmissionStrategy {
    if (orderSource === FmsOrderSource.COMMON_PLATFORM && cpFmsIntegrationEnabled) {
      return FmsOrderSubmissionStrategy(this.objectMapper, this.fmsClient, this.documentApiClient)
    }

    if (orderSource === FmsOrderSource.CEMO && cemoFmsIntegrationEnabled) {
      if (order.type === RequestType.VARIATION) {
        return FmsVariationSubmissionStrategy(this.objectMapper, this.fmsClient)
      }

      return FmsOrderSubmissionStrategy(this.objectMapper, this.fmsClient, this.documentApiClient)
    }

    return FmsDummySubmissionStrategy(this.objectMapper)
  }

  fun submitOrder(order: Order, orderSource: FmsOrderSource): FmsSubmissionResult {
    val strategy = this.getSubmissionStrategy(order, orderSource)
    val submissionResult = strategy.submitOrder(order, orderSource)

    repo.save(submissionResult)

    return submissionResult
  }
}
