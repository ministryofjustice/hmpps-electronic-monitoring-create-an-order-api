package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmitFmsOrderResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.SubmitFmsOrderResultRepository

@Service
@Configuration
class FmsService(
  val fmsClient: FmsClient,
  val objectMapper: ObjectMapper,
  val submitFmdOrderResultRepository: SubmitFmsOrderResultRepository,
  @Value("\${toggle.fms-integration.enabled:false}") val fmsIntegrationEnabled: Boolean,
) {

  fun submitOrder(order: Order, orderSource: FmsOrderSource): SubmitFmsOrderResult {
    val result = SubmitFmsOrderResult(id = order.id, orderSource = orderSource)

    if (fmsIntegrationEnabled) {
      try {
        // create FMS device wearer
        val fmsDeviceWearer = DeviceWearer.fromCemoOrder(order)
        result.fmsDeviceWearerRequest = objectMapper.writeValueAsString(fmsDeviceWearer)
        val createDeviceWearerResult = fmsClient.createDeviceWearer(fmsDeviceWearer, orderId = order.id)
        result.deviceWearerId = createDeviceWearerResult.result.first().id

        // create FMS monitoring order
        val fmsOrder = MonitoringOrder.fromOrder(order, result.deviceWearerId)
        result.fmsOrderRequest = objectMapper.writeValueAsString(fmsOrder)
        val createOrderResult = fmsClient.createMonitoringOrder(fmsOrder, orderId = order.id)
        result.fmsOrderId = createOrderResult.result.first().id
        // TODO: Upload attachments

        result.success = true
      } catch (e: CreateSercoEntityException) {
        result.success = false
        result.error = e.message
      }
    } else {
      val fmsDeviceWearer = DeviceWearer.fromCemoOrder(order)
      result.fmsDeviceWearerRequest = objectMapper.writeValueAsString(fmsDeviceWearer)
      result.deviceWearerId = order.id.toString()

      val fmsOrder = MonitoringOrder.fromOrder(order, result.deviceWearerId)
      result.fmsOrderRequest = objectMapper.writeValueAsString(fmsOrder)
      result.success = true
    }

    submitFmdOrderResultRepository.save(result)
    return result
  }
}
