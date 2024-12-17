package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmitFmsOrderResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRequestResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind

class FmsDummySubmissionStrategy(
  objectMapper: ObjectMapper,
) : FmsSubmissionStrategyBase(objectMapper) {

  private fun createDeviceWearer(order: Order): FmsRequestResult {
    val deviceWearerResult = this.getDeviceWearer(order)

    if (!deviceWearerResult.success) {
      return FmsRequestResult(
        success = false,
        error = deviceWearerResult.error.toString(),
      )
    }

    val deviceWearer = deviceWearerResult.data!!
    val serialiseResult = this.serialiseDeviceWearer(deviceWearer)

    if (!serialiseResult.success) {
      return FmsRequestResult(
        success = false,
        error = serialiseResult.error.toString(),
        id = "Fake Id",
      )
    }

    return FmsRequestResult(
      success = true,
      id = "Fake Id",
      payload = serialiseResult.data!!,
    )
  }

  private fun createMonitoringOrder(order: Order, deviceWearerId: String): FmsRequestResult {
    val monitoringOrderResult = this.getMonitoringOrder(order, deviceWearerId)

    if (!monitoringOrderResult.success) {
      return FmsRequestResult(
        success = false,
        error = monitoringOrderResult.error.toString(),
      )
    }

    val monitoringOrder = monitoringOrderResult.data!!
    val serialiseResult = this.serialiseMonitoringOrder(monitoringOrder)

    if (!serialiseResult.success) {
      return FmsRequestResult(
        success = false,
        error = serialiseResult.error.toString(),
        id = "Fake Id",
      )
    }

    return FmsRequestResult(
      success = true,
      id = "Fake Id",
      payload = serialiseResult.data!!,
    )
  }

  override fun submitOrder(order: Order, orderSource: FmsOrderSource): SubmitFmsOrderResult {
    val createDeviceWearerResult = this.createDeviceWearer(order)
    val deviceWearerId = createDeviceWearerResult.id
    val deviceWearerRequest = createDeviceWearerResult.payload

    if (!createDeviceWearerResult.success) {
      return SubmitFmsOrderResult(
        id = order.id,
        success = false,
        strategy = FmsSubmissionStrategyKind.DUMMY,
        error = createDeviceWearerResult.error,
        deviceWearerId = deviceWearerId,
        fmsDeviceWearerRequest = deviceWearerRequest,
        orderSource = orderSource,
      )
    }

    val createMonitoringOrderResult = this.createMonitoringOrder(order, deviceWearerId)
    val monitoringOrderId = createMonitoringOrderResult.id
    val monitoringOrderRequest = createMonitoringOrderResult.payload

    if (!createMonitoringOrderResult.success) {
      return SubmitFmsOrderResult(
        id = order.id,
        success = false,
        strategy = FmsSubmissionStrategyKind.DUMMY,
        error = createMonitoringOrderResult.error,
        deviceWearerId = deviceWearerId,
        fmsDeviceWearerRequest = deviceWearerRequest,
        fmsOrderId = monitoringOrderId,
        fmsOrderRequest = monitoringOrderRequest,
        orderSource = orderSource,
      )
    }

    return SubmitFmsOrderResult(
      id = order.id,
      success = true,
      strategy = FmsSubmissionStrategyKind.DUMMY,
      deviceWearerId = deviceWearerId,
      fmsDeviceWearerRequest = deviceWearerRequest,
      fmsOrderId = monitoringOrderId,
      fmsOrderRequest = monitoringOrderRequest,
      orderSource = orderSource,
    )
  }
}
