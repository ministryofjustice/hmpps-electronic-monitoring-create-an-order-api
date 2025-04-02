package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Result
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.DeviceWearerRequest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.DeviceWearerRequestOnlineFormAdaptor
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.MonitoringOrderRequest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.MonitoringOrderRequestOnlineFormAdaptor

abstract class FmsSubmissionStrategyBase(val objectMapper: ObjectMapper) : FmsSubmissionStrategy {

  protected fun getDeviceWearer(order: Order): Result<DeviceWearerRequest> = try {
    Result(
      success = true,
      data = DeviceWearerRequestOnlineFormAdaptor(order),
    )
  } catch (e: Exception) {
    Result(
      success = false,
      error = Exception("Failed to translate Order to FMS Device Wearer", e),
    )
  }

  protected fun serialiseDeviceWearer(deviceWearer: DeviceWearerRequest): Result<String> = try {
    Result(
      success = true,
      data = objectMapper.writeValueAsString(deviceWearer),
    )
  } catch (e: Exception) {
    Result(
      success = false,
      error = Exception("Failed to serialise FMS Device Wearer", e),
    )
  }

  protected fun getMonitoringOrder(order: Order, deviceWearerId: String): Result<MonitoringOrderRequest> {
    return try {
      return Result(
        success = true,
        data = MonitoringOrderRequestOnlineFormAdaptor(order, deviceWearerId),
      )
    } catch (e: Exception) {
      Result(
        success = false,
        error = Exception("Failed to translate Order to FMS Monitoring Order", e),
      )
    }
  }

  protected fun serialiseMonitoringOrder(order: MonitoringOrderRequest): Result<String> {
    return try {
      return Result(
        success = true,
        data = objectMapper.writeValueAsString(order),
      )
    } catch (e: Exception) {
      Result(
        success = false,
        error = Exception("Failed to serialise FMS Monitoring Order", e),
      )
    }
  }
}
