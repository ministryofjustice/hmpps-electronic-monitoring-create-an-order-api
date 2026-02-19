package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Result
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder

abstract class FmsSubmissionStrategyBase(val objectMapper: ObjectMapper, private val featureFlags: FeatureFlags) :
  FmsSubmissionStrategy {

  protected fun getDeviceWearer(order: Order): Result<DeviceWearer> = try {
    Result(
      success = true,
      data = DeviceWearer.fromCemoOrder(order, featureFlags),
    )
  } catch (e: Exception) {
    Result(
      success = false,
      error = Exception("Failed to translate Order to FMS Device Wearer", e),
    )
  }

  protected fun serialiseDeviceWearer(deviceWearer: DeviceWearer): Result<String> = try {
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

  protected fun getMonitoringOrder(order: Order, deviceWearerId: String): Result<MonitoringOrder> {
    return try {
      return Result(
        success = true,
        data = MonitoringOrder.fromOrder(order, deviceWearerId, featureFlags),
      )
    } catch (e: Exception) {
      Result(
        success = false,
        error = Exception("Failed to translate Order to FMS Monitoring Order", e),
      )
    }
  }

  protected fun serialiseMonitoringOrder(order: MonitoringOrder): Result<String> {
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
