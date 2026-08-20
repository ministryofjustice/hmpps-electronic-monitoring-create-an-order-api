package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsOrderDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsOrderDetailsRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsDummySubmissionStrategy
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsOrderSubmissionStrategy
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsSubmissionStrategy
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy.FmsVariationSubmissionStrategy
import java.util.UUID

@Service
@Configuration
@EnableConfigurationProperties(
  FeatureFlags::class,
)
class FmsService(
  val fmsClient: FmsClient,
  val documentApiClient: DocumentApiClient,
  val objectMapper: ObjectMapper,
  val repo: FmsSubmissionResultRepository,
  @param:Value("\${toggle.cemo.fms-integration.enabled:false}") val cemoFmsIntegrationEnabled: Boolean,
  @param:Value("\${toggle.common-platform.fms-integration.enabled:false}") val cpFmsIntegrationEnabled: Boolean,
  private val featureFlags: FeatureFlags,
  val eventService: EventService,
  val fmsOrderDetailsRepository: FmsOrderDetailsRepository,
) {

  private val orderSubmissionStrategy = FmsOrderSubmissionStrategy(
    this.objectMapper,
    this.fmsClient,
    this.documentApiClient,
    featureFlags,
  )

  private val variationSubmissionStrategy = FmsVariationSubmissionStrategy(
    this.objectMapper,
    this.fmsClient,
    this.documentApiClient,
    featureFlags,
    repo,
    fmsOrderDetailsRepository,
  )

  fun getLatestOrderVersion(order: Order): OrderVersion? {
    if (featureFlags.getApiEnabled) {
      val caseId = variationSubmissionStrategy.getOriginalNewOrderCaseId(order)
      if (!caseId.isNullOrBlank()) {
        try {
          val lastFmsDetail = fmsClient.getLatestOrderDetails(caseId)

          val result = lastFmsDetail.toOrderVersion(
            order.id,
            order.username,
            OrderStatus.IN_PROGRESS,
            type = order.type,
            dataDictionaryVersion = order.dataDictionaryVersion,
          )

          val orderDetails = FmsOrderDetails(
            caseId = lastFmsDetail.caseId,
            deviceWearerAsJson = objectMapper.writeValueAsString(lastFmsDetail.deviceWearer),
            monitoringOrderAsJson = objectMapper.writeValueAsString(lastFmsDetail.monitoringOrder),
          )

          fmsOrderDetailsRepository.save(orderDetails)
          return result
        } catch (ex: CreateSercoEntityException) {
          val errorMessage = ex.message ?: ""
          eventService.recordEvent(
            "Failed to retrieve latest order from FMS: $caseId",
            mapOf(
              "error" to errorMessage,
              "caseId" to caseId,
            ),
          )
        }
      }
    }
    return null
  }

  private fun getSubmissionStrategy(order: Order, orderSource: FmsOrderSource): FmsSubmissionStrategy {
    if (orderSource === FmsOrderSource.COMMON_PLATFORM && cpFmsIntegrationEnabled) {
      return orderSubmissionStrategy
    }

    if (orderSource === FmsOrderSource.CEMO && cemoFmsIntegrationEnabled) {
      if (RequestType.VARIATION_TYPES.contains(order.type)) {
        return variationSubmissionStrategy
      }

      return orderSubmissionStrategy
    }

    return FmsDummySubmissionStrategy(this.objectMapper, featureFlags)
  }

  fun submitOrder(order: Order, orderSource: FmsOrderSource): FmsSubmissionResult {
    val strategy = this.getSubmissionStrategy(order, orderSource)
    val submissionResult = strategy.submitOrder(order, orderSource)

    repo.save(submissionResult)

    return submissionResult
  }

  fun getFmsDeviceWearerSubmissionResultById(resultId: UUID): String =
    repo.findById(resultId).orElseThrow().deviceWearerResult.payload

  fun getFmsMonitoringOrderSubmissionResultByOrderId(resultId: UUID): String =
    repo.findById(resultId).orElseThrow().monitoringOrderResult.payload
}
