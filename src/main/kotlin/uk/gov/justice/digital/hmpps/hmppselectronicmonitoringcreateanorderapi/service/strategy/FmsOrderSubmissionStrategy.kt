package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmitFmsOrderResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind

@Component
class FmsOrderSubmissionStrategy : FmsSubmissionStrategy {

  @Autowired
  private lateinit var fmsClient: FmsClient

  private fun createDeviceWearer(order: Order): Result<String> {
    return Result.failure(
      Exception("Not implemented"),
    )
  }

  private fun createMonitoringOrder(order: Order): Result<String> {
    return Result.failure(
      Exception("Not implemented"),
    )
  }

  override fun submitOrder(order: Order, orderSource: FmsOrderSource): SubmitFmsOrderResult {
    val createDeviceWearerResult = this.createDeviceWearer(order)

    if (createDeviceWearerResult.isFailure) {
      return SubmitFmsOrderResult(
        success = false,
        strategy = FmsSubmissionStrategyKind.ORDER,
        error = "Failed to create the device wearer",
        deviceWearerId = "",
        fmsDeviceWearerRequest = "",
        fmsOrderId = "",
        fmsOrderRequest = "",
        orderSource = orderSource,
      )
    }

    val createMonitoringOrderResult = this.createMonitoringOrder(order)

    if (createMonitoringOrderResult.isFailure) {
      return SubmitFmsOrderResult(
        success = false,
        strategy = FmsSubmissionStrategyKind.ORDER,
        error = "Failed to create the monitoring order",
        deviceWearerId = createDeviceWearerResult.getOrDefault(""),
        fmsDeviceWearerRequest = "",
        fmsOrderId = "",
        fmsOrderRequest = "",
        orderSource = orderSource,
      )
    }

    return SubmitFmsOrderResult(
      success = true,
      strategy = FmsSubmissionStrategyKind.ORDER,
      error = "",
      deviceWearerId = createDeviceWearerResult.getOrDefault(""),
      fmsDeviceWearerRequest = "",
      fmsOrderId = createMonitoringOrderResult.getOrDefault(""),
      fmsOrderRequest = "",
      orderSource = orderSource,
    )
  }
}
