package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Result
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsOrderDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.compareTo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsOrderDetailsRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import java.util.*

class FmsVariationSubmissionStrategy(
  objectMapper: ObjectMapper,
  val fmsClient: FmsClient,
  val documentApiClient: DocumentApiClient,
  private val featureFlags: FeatureFlags,
  val repo: FmsSubmissionResultRepository,
  private val fmsOrderDetailsRepository: FmsOrderDetailsRepository,
) : FmsSubmissionStrategyBase(objectMapper, featureFlags) {

  private fun submitUpdateDeviceWearerRequest(
    deviceWearerPayload: String,
    orderId: UUID,
    orderSource: FmsOrderSource,
  ): Result<String> = try {
    Result(
      success = true,
      data = fmsClient.updateDeviceWearer(deviceWearerPayload, orderId, orderSource).result.first().id,
    )
  } catch (e: Exception) {
    Result(
      success = false,
      error = Exception("Failed to submit FMS Device Wearer", e),
    )
  }

  private fun submitUpdateMonitoringOrderRequest(
    monitoringOrderPayload: String,
    orderId: UUID,
    orderSource: FmsOrderSource,
  ): Result<String> = try {
    Result(
      success = true,
      data = fmsClient.updateMonitoringOrder(monitoringOrderPayload, orderId, orderSource).result.first().id,
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

  private fun updateDeviceWearer(order: Order, orderSource: FmsOrderSource): FmsDeviceWearerSubmissionResult {
    val deviceWearerResult = this.getDeviceWearer(order, orderSource)

    if (!deviceWearerResult.success) {
      return FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = deviceWearerResult.error.toString(),
      )
    }

    val deviceWearer = deviceWearerResult.data!!
    if (featureFlags.getApiEnabled) {
      deviceWearer.newOrderCaseId = getOriginalNewOrderCaseId(order) ?: ""
    }
    val serialiseResult = this.serialiseDeviceWearer(deviceWearer)

    if (!serialiseResult.success) {
      return FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = serialiseResult.error.toString(),
      )
    }

    val submissionResult = this.submitUpdateDeviceWearerRequest(serialiseResult.data!!, order.id, orderSource)

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
    val monitoringOrderResult = this.getMonitoringOrder(order, deviceWearerId, orderSource)

    if (!monitoringOrderResult.success) {
      return FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = monitoringOrderResult.error.toString(),
      )
    }

    val monitoringOrder = monitoringOrderResult.data!!

    if (orderSource == FmsOrderSource.CEMO) {
      val caseId = getOriginalNewOrderCaseId(order)
      val latestFmsDetails = caseId?.takeIf { it.isNotBlank() }
        ?.let { fmsOrderDetailsRepository.findById(it).orElse(null) }
      val previousMonitoringOrderJson = latestFmsDetails?.monitoringOrderAsJson
        ?: lastSuccessfulSubmitResult?.monitoringOrderResult?.payload
      val previousMonitoringOrder = previousMonitoringOrderJson?.let {
        objectMapper.readValue(it, MonitoringOrder::class.java)
      }
      previousMonitoringOrder?.let { preserveZoneConditionTimes(monitoringOrder, it) }
      val orderChanges =
        calculateOrderChanges(
          monitoringOrder,
          submitDeviceWearerResult,
          lastSuccessfulSubmitResult,
          latestFmsDetails,
          previousMonitoringOrder,
        )
      monitoringOrder.orderVariationDetails = orderChanges.variationDetails
      if (order.type == RequestType.VARIATION) {
        monitoringOrder.orderVariationType = orderChanges.variationType.value
      } else {
        monitoringOrder.orderVariationType = VariationType.OTHER.value
      }
    } else {
      monitoringOrder.orderVariationDetails = order.variationDetails?.variationDetails ?: ""
    }

    val serialiseResult = this.serialiseMonitoringOrder(monitoringOrder)

    if (!serialiseResult.success) {
      return FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.FAILURE,
        error = serialiseResult.error.toString(),
      )
    }

    val submissionResult = this.submitUpdateMonitoringOrderRequest(serialiseResult.data!!, order.id, orderSource)

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

  private fun calculateOrderChanges(
    monitoringOrder: MonitoringOrder,
    submitDeviceWearerResult: FmsDeviceWearerSubmissionResult,
    lastSuccessfulSubmitResult: FmsSubmissionResult?,
    latestFmsDetails: FmsOrderDetails?,
    previousMonitoringOrder: MonitoringOrder?,
  ): OrderChanges {
    if (lastSuccessfulSubmitResult == null && latestFmsDetails == null) {
      val details = buildString {
        appendLine("User entered:")
        appendLine(monitoringOrder.orderVariationDetails)
      }
      return OrderChanges(details, VariationType.OTHER)
    }

    val deviceWearerJson =
      latestFmsDetails?.deviceWearerAsJson ?: lastSuccessfulSubmitResult!!.deviceWearerResult.payload
    val currentDeviceWearer = objectMapper.readValue(submitDeviceWearerResult.payload, DeviceWearer::class.java)
    val lastDeviceWearer = objectMapper.readValue(
      deviceWearerJson,
      DeviceWearer::class.java,
    )
    val lastMonitoringOrder = requireNotNull(previousMonitoringOrder)

    val deviceWearerChanges = currentDeviceWearer.compareTo(lastDeviceWearer)
    val monitoringOrderChanges = monitoringOrder.compareTo(lastMonitoringOrder)

    val changeDetails = buildString {
      appendLine("User entered:")
      appendLine(monitoringOrder.orderVariationDetails)

      if (deviceWearerChanges.messages.isNotEmpty() || monitoringOrderChanges.messages.isNotEmpty()) {
        appendLine("CEMO determined changes:")
        deviceWearerChanges.messages.forEach(this::appendLine)
        monitoringOrderChanges.messages.forEach(this::appendLine)
      }
    }

    val variationType =
      listOf(deviceWearerChanges.orderVariationType, monitoringOrderChanges.orderVariationType).minBy { it.priority }

    return OrderChanges(changeDetails, variationType)
  }

  private fun preserveZoneConditionTimes(current: MonitoringOrder, previous: MonitoringOrder) {
    val zoneConditionNames = setOf("EM Exclusion / Inclusion Zone", "Restriction Zones")
    val previousConditions = previous.enforceableCondition.orEmpty().associateBy { it.condition }
    current.enforceableCondition = current.enforceableCondition.orEmpty().map { condition ->
      if (condition.condition !in zoneConditionNames) return@map condition
      val previousCondition = previousConditions[condition.condition] ?: return@map condition
      condition.copy(
        startDate = preserveTimePart(condition.startDate, previousCondition.startDate),
        endDate = preserveTimePart(condition.endDate, previousCondition.endDate),
      )
    }.toMutableList()
  }

  private fun preserveTimePart(current: String?, previous: String?): String? {
    if (current.isNullOrBlank() || previous.isNullOrBlank()) return current

    val time = previous.substringAfter(' ', missingDelimiterValue = "")
    if (time.isBlank()) return current

    return "${current.substringBefore(' ')} $time"
  }

  // Every version shares one external case, so its state can't tell which submission was cancelled
  private fun getLastSuccessfulSubmissionResult(order: Order): FmsSubmissionResult? = order.versions
    .filter { version ->
      version.fmsResultId != null &&
        version.status == OrderStatus.SUBMITTED &&
        version.statusUpdates.none { it.status == ProcessingStatus.REJECTED }
    }
    .maxByOrNull { it.versionId }
    ?.let { repo.getReferenceById(it.fmsResultId!!) }

  fun getOriginalNewOrderCaseId(order: Order): String? = order.versions
    .filter {
      it.fmsResultId != null &&
        it.status == OrderStatus.SUBMITTED &&
        (it.type == RequestType.REQUEST || it.type == RequestType.AMEND_ORIGINAL_REQUEST)
    }
    .minByOrNull { it.versionId }
    ?.let { repo.getReferenceById(it.fmsResultId!!) }
    ?.caseId

  override fun submitOrder(order: Order, orderSource: FmsOrderSource): FmsSubmissionResult {
    val createDeviceWearerResult = this.updateDeviceWearer(order, orderSource)
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

data class OrderChanges(val variationDetails: String, val variationType: VariationType)
