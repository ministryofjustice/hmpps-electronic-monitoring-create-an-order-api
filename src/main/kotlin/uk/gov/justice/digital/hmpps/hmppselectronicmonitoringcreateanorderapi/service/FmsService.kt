package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmitFmsOrderResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.SubmitFmsOrderResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.AdditionalDocumentService

@Service
@Configuration
class FmsService(
  val fmsClient: FmsClient,
  val webClient: DocumentApiClient,
  val objectMapper: ObjectMapper,
  val submitFmsOrderResultRepository: SubmitFmsOrderResultRepository,
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

        // Create attachment/s

        // 1. Get a list of the documents related to this order.
        val additionalDocuments = order.additionalDocuments.map { document ->
          webClient.getDocument(document.id.toString())
        }

        // 2. For each document in the list,
          // b. Convert it to binary data (if required; may already be appropriate format?)
          // c. Submit it to the Serco API endpoint
          // d, e. Handle SUCCESS & FAILURE:
            // i. SUCCESS: Update CEMO DB
            // ii. FAILURE: Define path for this: Throw suitable error, which is caught by the existing catch (below).

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

    submitFmsOrderResultRepository.save(result)
    return result
  }
}
