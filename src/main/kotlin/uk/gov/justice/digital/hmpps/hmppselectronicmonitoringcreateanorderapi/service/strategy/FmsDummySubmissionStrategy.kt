package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind

class FmsDummySubmissionStrategy(
  objectMapper: ObjectMapper,
) : FmsSubmissionStrategyBase(objectMapper) {

  private fun createDeviceWearer(order: Order): FmsDeviceWearerSubmissionResult {
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

    return FmsDeviceWearerSubmissionResult(
      status = SubmissionStatus.SUCCESS,
      payload = serialiseResult.data!!,
    )
  }

  private fun createMonitoringOrder(order: Order, deviceWearerId: String): FmsMonitoringOrderSubmissionResult {
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

    return FmsMonitoringOrderSubmissionResult(
      status = SubmissionStatus.SUCCESS,
      payload = serialiseResult.data!!,
    )
  }

  override fun submitOrder(order: Order, orderSource: FmsOrderSource): FmsSubmissionResult {
    val createDeviceWearerResult = this.createDeviceWearer(order)
    val deviceWearerId = createDeviceWearerResult.deviceWearerId

    if (createDeviceWearerResult.status == SubmissionStatus.FAILURE) {
      return FmsSubmissionResult(
        orderId = order.id,
        strategy = FmsSubmissionStrategyKind.DUMMY,
        deviceWearerResult = createDeviceWearerResult,
        orderSource = orderSource,
      )
    }

    val createMonitoringOrderResult = this.createMonitoringOrder(order, deviceWearerId)

    return FmsSubmissionResult(
      orderId = order.id,
      strategy = FmsSubmissionStrategyKind.DUMMY,
      deviceWearerResult = createDeviceWearerResult,
      monitoringOrderResult = createMonitoringOrderResult,
      orderSource = orderSource,
    )
  }
}
