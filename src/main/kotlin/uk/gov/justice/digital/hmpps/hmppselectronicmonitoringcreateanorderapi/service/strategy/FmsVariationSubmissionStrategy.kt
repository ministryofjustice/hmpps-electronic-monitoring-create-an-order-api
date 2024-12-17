package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmitFmsOrderResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind

@Component
class FmsVariationSubmissionStrategy() : FmsSubmissionStrategy {

  @Autowired
  lateinit var fmsClient: FmsClient

  private fun updateDeviceWearer(order: Order): Result<String> {
    return Result.failure(
      Exception("Not implemented"),
    )
  }

  private fun updateMonitoringOrder(order: Order): Result<String> {
    return Result.failure(
      Exception("Not implemented"),
    )
  }

  override fun submitOrder(order: Order, orderSource: FmsOrderSource): SubmitFmsOrderResult {
    val updateDeviceWearerResult = this.updateDeviceWearer(order)

    if (updateDeviceWearerResult.isFailure) {
      return SubmitFmsOrderResult(
        success = false,
        strategy = FmsSubmissionStrategyKind.VARIATION,
        error = "Failed to update the device wearer",
        deviceWearerId = "",
        fmsDeviceWearerRequest = "",
        fmsOrderRequest = "",
        fmsOrderId = "",
        orderSource = orderSource,
      )
    }

    val updateMonitoringOrderResult = this.updateMonitoringOrder(order)

    if (updateMonitoringOrderResult.isFailure) {
      return SubmitFmsOrderResult(
        success = false,
        strategy = FmsSubmissionStrategyKind.VARIATION,
        error = "Failed to update the monitoring order",
        deviceWearerId = updateDeviceWearerResult.getOrDefault(""),
        fmsDeviceWearerRequest = "",
        fmsOrderId = "",
        fmsOrderRequest = "",
        orderSource = orderSource,
      )
    }

    return SubmitFmsOrderResult(
      success = true,
      strategy = FmsSubmissionStrategyKind.VARIATION,
      error = "",
      deviceWearerId = updateDeviceWearerResult.getOrDefault(""),
      fmsDeviceWearerRequest = "",
      fmsOrderId = updateDeviceWearerResult.getOrDefault(""),
      fmsOrderRequest = "",
      orderSource = orderSource,
    )
  }
}
