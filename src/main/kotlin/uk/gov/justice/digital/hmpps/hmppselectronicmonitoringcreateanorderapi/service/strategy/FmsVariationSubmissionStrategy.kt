package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Result
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
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

  private fun updateDeviceWearer(order: Order): FmsDeviceWearerSubmissionResult {
    val deviceWearerResult = this.getDeviceWearer(order)

    if (!deviceWearerResult.success) {
      return FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = deviceWearerResult.error.toString(),
      )
    }

    val deviceWearer = deviceWearerResult.data!!
    val serialiseResult = this.serialiseDeviceWearer(deviceWearer)

    if (!serialiseResult.success) {
      return FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = serialiseResult.error.toString(),
      )
    }

    val submissionResult = this.submitUpdateDeviceWearerRequest(deviceWearer, order.id)

    if (!submissionResult.success) {
      return FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = submissionResult.error.toString(),
        payload = serialiseResult.data!!,
      )
    }

    return FmsDeviceWearerSubmissionResult(
      status = SubmissionStatus.SUCCESS,
      deviceWearerId = submissionResult.data!!,
      payload = serialiseResult.data!!,
    )
  }

  private fun updateMonitoringOrder(order: Order, deviceWearerId: String): FmsMonitoringOrderSubmissionResult {
    val monitoringOrderResult = this.getMonitoringOrder(order, deviceWearerId)

    if (!monitoringOrderResult.success) {
      return FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = monitoringOrderResult.error.toString(),
      )
    }

    val monitoringOrder = monitoringOrderResult.data!!
    val serialiseResult = this.serialiseMonitoringOrder(monitoringOrder)

    if (!serialiseResult.success) {
      return FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = serialiseResult.error.toString(),
      )
    }

    val submissionResult = this.submitUpdateMonitoringOrderRequest(monitoringOrder, order.id)

    if (!submissionResult.success) {
      return FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = submissionResult.error.toString(),
        payload = serialiseResult.data!!,
      )
    }

    return FmsMonitoringOrderSubmissionResult(
      status = SubmissionStatus.SUCCESS,
      monitoringOrderId = submissionResult.data!!,
      payload = serialiseResult.data!!,
    )
  }

  override fun submitOrder(order: Order, orderSource: FmsOrderSource): FmsSubmissionResult {
    val createDeviceWearerResult = this.updateDeviceWearer(order)
    val deviceWearerId = createDeviceWearerResult.deviceWearerId

    if (createDeviceWearerResult.status === SubmissionStatus.FAILURE) {
      return FmsSubmissionResult(
        orderId = order.id,
        strategy = FmsSubmissionStrategyKind.VARIATION,
        deviceWearerResult = createDeviceWearerResult,
        orderSource = orderSource,
      )
    }

    val createMonitoringOrderResult = this.updateMonitoringOrder(order, deviceWearerId)

    return FmsSubmissionResult(
      orderId = order.id,
      strategy = FmsSubmissionStrategyKind.VARIATION,
      deviceWearerResult = createDeviceWearerResult,
      monitoringOrderResult = createMonitoringOrderResult,
      orderSource = orderSource,
    )
  }
}
