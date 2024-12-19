package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmitFmsOrderResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRequestResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Result
import java.util.*

class FmsVariationSubmissionStrategy(
  objectMapper: ObjectMapper,
  val fmsClient: FmsClient,
) : FmsSubmissionStrategyBase(objectMapper) {

  private fun submitUpdateDeviceWearerRequest(deviceWearer: DeviceWearer, orderId: UUID): Result<String> {
    return try {
      // TODO: Should call updateDeviceWearer endpoint, but not currently possible
      Result(
        success = true,
        data = fmsClient.createDeviceWearer(deviceWearer, orderId).result.first().id,
      )
    } catch (e: Exception) {
      Result(
        success = false,
        error = Exception("Failed to submit FMS Device Wearer", e),
      )
    }
  }

  private fun submitUpdateMonitoringOrderRequest(monitoringOrder: MonitoringOrder, orderId: UUID): Result<String> {
    return try {
      Result(
        success = true,
        data = fmsClient.updateMonitoringOrder(monitoringOrder, orderId).result.first().id,
      )
    } catch (e: Exception) {
      Result(
        success = false,
        error = Exception("Failed to submit FMS Monitoring Order", e),
      )
    }
  }

  private fun updateDeviceWearer(order: Order): FmsRequestResult {
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
      )
    }

    val submissionResult = this.submitUpdateDeviceWearerRequest(deviceWearer, order.id)

    if (!submissionResult.success) {
      return FmsRequestResult(
        success = false,
        error = submissionResult.error.toString(),
        payload = submissionResult.data!!,
      )
    }

    return FmsRequestResult(
      success = true,
      id = submissionResult.data!!,
      payload = serialiseResult.data!!,
    )
  }

  private fun updateMonitoringOrder(order: Order, deviceWearerId: String): FmsRequestResult {
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
      )
    }

    val submissionResult = this.submitUpdateMonitoringOrderRequest(monitoringOrder, order.id)

    if (!submissionResult.success) {
      return FmsRequestResult(
        success = false,
        error = submissionResult.error.toString(),
        payload = serialiseResult.data!!,
      )
    }

    return FmsRequestResult(
      success = true,
      id = submissionResult.data!!,
      payload = serialiseResult.data!!,
    )
  }

  override fun submitOrder(order: Order, orderSource: FmsOrderSource): SubmitFmsOrderResult {
    val createDeviceWearerResult = this.updateDeviceWearer(order)
    val deviceWearerId = createDeviceWearerResult.id
    val deviceWearerRequest = createDeviceWearerResult.payload

    if (!createDeviceWearerResult.success) {
      return SubmitFmsOrderResult(
        id = order.id,
        success = false,
        strategy = FmsSubmissionStrategyKind.ORDER,
        error = createDeviceWearerResult.error,
        deviceWearerId = deviceWearerId,
        fmsDeviceWearerRequest = deviceWearerRequest,
        orderSource = orderSource,
      )
    }

    val createMonitoringOrderResult = this.updateMonitoringOrder(order, deviceWearerId)
    val monitoringOrderId = createMonitoringOrderResult.id
    val monitoringOrderRequest = createMonitoringOrderResult.payload

    if (!createMonitoringOrderResult.success) {
      return SubmitFmsOrderResult(
        id = order.id,
        success = false,
        strategy = FmsSubmissionStrategyKind.ORDER,
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
      strategy = FmsSubmissionStrategyKind.ORDER,
      deviceWearerId = deviceWearerId,
      fmsDeviceWearerRequest = deviceWearerRequest,
      fmsOrderId = monitoringOrderId,
      fmsOrderRequest = monitoringOrderRequest,
      orderSource = orderSource,
    )
  }
}
