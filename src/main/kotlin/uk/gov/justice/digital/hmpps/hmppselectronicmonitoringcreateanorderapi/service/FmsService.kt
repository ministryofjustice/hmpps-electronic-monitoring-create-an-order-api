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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.SubmitFmsOrderResultRepository

@Service
@Configuration
class FmsService(
  val fmsClient: FmsClient,
  val documentApiWebClient: DocumentApiClient,
  val objectMapper: ObjectMapper,
  val submitFmsOrderResultRepository: SubmitFmsOrderResultRepository,
  @Value("\${toggle.fms-integration.enabled:false}") val fmsIntegrationEnabled: Boolean,
) {

  fun submitOrder(order: Order, orderSource: FmsOrderSource): SubmitFmsOrderResult {
    val result = SubmitFmsOrderResult(id = order.id, orderSource = orderSource)

    if (fmsIntegrationEnabled) {
      try {
        // create FMS device wearer
        val fmsDeviceWearer = DeviceWearer.fromCemoOrder(order) // 1. prepare FMS request - convert DW info to FMS DW request
        result.fmsDeviceWearerRequest = objectMapper.writeValueAsString(fmsDeviceWearer) // 2. prepare our result object - add the request to the FMS result object
        val createDeviceWearerResult = fmsClient.createDeviceWearer(fmsDeviceWearer, orderId = order.id) // 3. make FMS request - send request to create
        result.deviceWearerId = createDeviceWearerResult.result.first().id // 4. update our result object - add fms id to result object

        // create FMS monitoring order
        val fmsOrder = MonitoringOrder.fromOrder(order, result.deviceWearerId)
        result.fmsOrderRequest = objectMapper.writeValueAsString(fmsOrder)
        val createOrderResult = fmsClient.createMonitoringOrder(fmsOrder, orderId = order.id)
        result.fmsOrderId = createOrderResult.result.first().id

        // -------------------------
        // TODO: CREATE FMS ATTACHMENTS
        // 1. Prepare FMS Attachment request - Get first document metadata and file itself related to order
          val firstAttachmentMetadata = order.additionalDocuments.first()
          val firstAttachmentFileStream = documentApiWebClient.getDocument(firstAttachmentMetadata.id.toString())?.body?.blockFirst() // TODO: handle case where it's null

        // 2. Prepare result object - TODO: is this necessary?

        // 3. Make FMS request - send request to create - TODO: change caseId to relevant ID
          val createAttachmentResult = fmsClient.createAttachment(fileName = firstAttachmentMetadata.fileName!!, caseId = order.id, file = firstAttachmentFileStream!!, documentType = firstAttachmentMetadata.fileType.toString())

        // 4.


          // for full list:
          //   val additionalDocuments = order.additionalDocuments.map { document ->
          //   webClient.getDocument(document.id.toString())
          // }

        // combine all responses into a json string


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
