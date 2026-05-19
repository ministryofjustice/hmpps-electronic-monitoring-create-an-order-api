package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Result
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.compareTo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import java.util.*

class FmsVariationSubmissionStrategy(
  objectMapper: ObjectMapper,
  val fmsClient: FmsClient,
  val documentApiClient: DocumentApiClient,
  private val featureFlags: FeatureFlags,
  val repo: FmsSubmissionResultRepository,
) : FmsSubmissionStrategyBase(objectMapper, featureFlags) {

  private fun submitUpdateDeviceWearerRequest(deviceWearerPayload: String, orderId: UUID): Result<String> = try {
    Result(
      success = true,
      data = fmsClient.updateDeviceWearer(deviceWearerPayload, orderId).result.first().id,
    )
  } catch (e: Exception) {
    Result(
      success = false,
      error = Exception("Failed to submit FMS Device Wearer", e),
    )
  }

  private fun submitUpdateMonitoringOrderRequest(monitoringOrderPayload: String, orderId: UUID): Result<String> = try {
    Result(
      success = true,
      data = fmsClient.updateMonitoringOrder(monitoringOrderPayload, orderId).result.first().id,
    )
  } catch (e: Exception) {
    Result(
      success = false,
      error = Exception("Failed to submit FMS Monitoring Order", e),
    )
  }

  private fun createAttachment(document: AdditionalDocument, deviceWearerId: String): FmsAttachmentSubmissionResult {
    try {
      val fileId = document.documentId.toString()
      val fileName = document.fileName
      val fileType = document.fileType.toString()
      val fileStream = this.documentApiClient.getDocument(fileId)?.body?.blockFirst()
      val result = fmsClient.createAttachment(
        fileName = fileName!!,
        caseId = deviceWearerId,
        file = fileStream!!,
        documentType = fileType,
        orderRequestType = RequestType.VARIATION,
      )

      return FmsAttachmentSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        sysId = result.result.sysId,
        fileType = fileType,
        attachmentId = fileId,
      )
    } catch (e: Exception) {
      return FmsAttachmentSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = Exception("Failed to submit FMS Attachment", e).toString(),
      )
    }
  }

  private fun createAttachments(order: Order, deviceWearerId: String): List<FmsAttachmentSubmissionResult> {
    val documents = order.additionalDocuments.toMutableList()

    if (order.enforcementZoneConditions.isNotEmpty()) {
      documents.addAll(
        order.enforcementZoneConditions.filter {
          it.fileId !== null && it.fileName !== null
        }.map {
          AdditionalDocument(
            id = it.fileId!!,
            versionId = order.getCurrentVersion().id,
            fileType = DocumentType.ENFORCEMENT_ZONE_MAP,
            fileName = it.fileName!!,
            documentId = it.fileId!!,
          )
        },
      )
    }

    return documents.map { this.createAttachment(it, deviceWearerId) }
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

    val submissionResult = this.submitUpdateDeviceWearerRequest(serialiseResult.data!!, order.id)

    if (!submissionResult.success) {
      return FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = submissionResult.error.toString(),
        payload = serialiseResult.data,
      )
    }

    return FmsDeviceWearerSubmissionResult(
      status = SubmissionStatus.SUCCESS,
      deviceWearerId = submissionResult.data!!,
      payload = serialiseResult.data,
    )
  }

  private fun updateMonitoringOrder(
    order: Order,
    deviceWearerId: String,
    submitDeviceWearerResult: FmsDeviceWearerSubmissionResult,
    lastSuccessfulSubmitResult: FmsSubmissionResult?,
    orderSource: FmsOrderSource,
  ): FmsMonitoringOrderSubmissionResult {
    val monitoringOrderResult = this.getMonitoringOrder(order, deviceWearerId,orderSource)

    if (!monitoringOrderResult.success) {
      return FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = monitoringOrderResult.error.toString(),
      )
    }

    val monitoringOrder = monitoringOrderResult.data!!

    monitoringOrder.orderVariationDetails =
      getListOfChanges(monitoringOrder, submitDeviceWearerResult, lastSuccessfulSubmitResult)

    val serialiseResult = this.serialiseMonitoringOrder(monitoringOrder)

    if (!serialiseResult.success) {
      return FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = serialiseResult.error.toString(),
      )
    }

    val submissionResult = this.submitUpdateMonitoringOrderRequest(serialiseResult.data!!, order.id)

    if (!submissionResult.success) {
      return FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = submissionResult.error.toString(),
        payload = serialiseResult.data,
      )
    }

    return FmsMonitoringOrderSubmissionResult(
      status = SubmissionStatus.SUCCESS,
      monitoringOrderId = submissionResult.data!!,
      payload = serialiseResult.data,
    )
  }

  private fun getListOfChanges(
    monitoringOrder: MonitoringOrder,
    submitDeviceWearerResult: FmsDeviceWearerSubmissionResult,
    lastSuccessfulSubmitResult: FmsSubmissionResult?,
  ): String {
    val changeDetails = StringBuilder()
    changeDetails.appendLine("User entered:")
    changeDetails.appendLine(monitoringOrder.orderVariationDetails)

    if (lastSuccessfulSubmitResult != null) {
      changeDetails.appendLine("CEMO determined changes:")

      val deviceWearerPayload = objectMapper.readValue(submitDeviceWearerResult.payload, DeviceWearer::class.java)
      val lastSuccessfulDeviceWearerPayload = objectMapper.readValue(
        lastSuccessfulSubmitResult.deviceWearerResult.payload,
        DeviceWearer::class.java,
      )
      deviceWearerPayload.compareTo(lastSuccessfulDeviceWearerPayload).forEach { change ->
        changeDetails.appendLine(change)
      }

      val lastSuccessFulMonitoringOrderPayload = objectMapper.readValue(
        lastSuccessfulSubmitResult.monitoringOrderResult.payload,
        MonitoringOrder::class.java,
      )
      monitoringOrder.compareTo(lastSuccessFulMonitoringOrderPayload).forEach { change ->
        changeDetails.appendLine(change)
      }
    }
    return changeDetails.toString()
  }

  private fun getLastSuccessfulSubmissionResult(order: Order): FmsSubmissionResult? {
    order.versions
      .filter { it.fmsResultId != null && it.status == OrderStatus.SUBMITTED }
      .sortedByDescending { it.versionId }
      .forEach { version ->
        val submissionResult = repo.getReferenceById(version.fmsResultId!!)
        val state = fmsClient.getState(submissionResult.deviceWearerResult.deviceWearerId)
        if (state != CaseState.CANCELLED && state != CaseState.UNKNOWN) {
          return submissionResult
        }
      }
    return null
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
    val lastSubmissionResult = this.getLastSuccessfulSubmissionResult(order)
    val createMonitoringOrderResult = this.updateMonitoringOrder(
      order,
      deviceWearerId,
      createDeviceWearerResult,
      lastSubmissionResult,
      orderSource,
    )

    if (createMonitoringOrderResult.status == SubmissionStatus.FAILURE) {
      return FmsSubmissionResult(
        orderId = order.id,
        strategy = FmsSubmissionStrategyKind.ORDER,
        deviceWearerResult = createDeviceWearerResult,
        monitoringOrderResult = createMonitoringOrderResult,
        orderSource = orderSource,
      )
    }

    val createAttachmentsResult = this.createAttachments(order, deviceWearerId)

    return FmsSubmissionResult(
      orderId = order.id,
      strategy = FmsSubmissionStrategyKind.VARIATION,
      deviceWearerResult = createDeviceWearerResult,
      monitoringOrderResult = createMonitoringOrderResult,
      attachmentResults = createAttachmentsResult.toMutableList(),
      orderSource = orderSource,
    )
  }
}
