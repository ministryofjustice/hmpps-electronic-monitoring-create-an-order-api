package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.util.UUID

@ActiveProfiles("test")
class FmsVariationSubmissionStrategyTest {
  private lateinit var strategy: FmsVariationSubmissionStrategy
  private lateinit var mockClient: FmsClient
  private lateinit var mockDocumentApiClient: DocumentApiClient
  private lateinit var objectMapper: ObjectMapper
  private lateinit var repo: FmsSubmissionResultRepository
  private val mockFeatureFlags =
    FeatureFlags(
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
      ddV6CourtMappings = false,
      deviceWearerPayloadVersion = DeviceWearerPayloadVersion.Prod,
    )
  private val mockOrderId = UUID.randomUUID()
  private val mockOrderVersionId = UUID.randomUUID()

  @BeforeEach
  fun setUp() {
    mockClient = mock(FmsClient::class.java)
    mockDocumentApiClient = mock(DocumentApiClient::class.java)
    objectMapper = jacksonObjectMapper()
    repo = mock(FmsSubmissionResultRepository::class.java)

    whenever(mockClient.updateDeviceWearer(any(), any())).thenReturn(
      FmsResponse(
        result = listOf(
          FmsResult(message = "mock response", id = "3"),
        ),
        status = "200",
      ),
    )
    whenever(mockClient.updateMonitoringOrder(any(), any())).thenReturn(
      FmsResponse(
        result = listOf(
          FmsResult(message = "mock response", id = "1"),
        ),
        status = "200",
      ),
    )

    whenever(mockClient.createAttachment(any(), any(), any(), any(), any())).thenReturn(
      FmsAttachmentResponse(FmsAttachmentResult(), status = "200"),
    )
    strategy =
      FmsVariationSubmissionStrategy(objectMapper, mockClient, mockDocumentApiClient, mockFeatureFlags, repo)
  }

  @Test
  fun `Should not set CEMO determined changes when there is no previous submitted version`() {
    val order = TestUtilities.createReadyToSubmitOrder(
      mockOrderId,
      mockOrderVersionId,
      requestType = RequestType.VARIATION,
    )
    order.additionalDocuments.clear()
    order.enforcementZoneConditions.clear()
    val result = strategy.submitOrder(order, FmsOrderSource.CEMO)
    assertThat(result.success).isTrue
    assertThat(result.monitoringOrderResult.payload).contains("User entered:")
    assertThat(result.monitoringOrderResult.payload).contains("Change to address")
    assertThat(result.monitoringOrderResult.payload).doesNotContain("CEMO determined changes:")
  }

  @Test
  fun `Should find last submitted version and generate CEMO determined changes`() {
    val mockSubmissionResultId = UUID.randomUUID()
    val order = TestUtilities.createReadyToSubmitOrder(
      mockOrderId,
      mockOrderVersionId,
      requestType = RequestType.REQUEST,
      status = OrderStatus.SUBMITTED,
      fmsResultId = mockSubmissionResultId,
    )

    whenever(repo.getReferenceById(mockSubmissionResultId)).thenReturn(
      FmsSubmissionResult(
        id = mockSubmissionResultId,
        orderId = mockOrderId,
        strategy = FmsSubmissionStrategyKind.VARIATION,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(
          payload = objectMapper.writeValueAsString(DeviceWearer.fromCemoOrder(order, mockFeatureFlags)),
          deviceWearerId = "1",
        ),
        monitoringOrderResult = FmsMonitoringOrderSubmissionResult(
          payload = objectMapper.writeValueAsString(MonitoringOrder.fromOrder(order, "1", mockFeatureFlags,
            FmsOrderSource.CEMO)),
        ),
      ),
    )
    val newOrder = TestUtilities.createReadyToSubmitOrder(
      mockOrderId,
      mockOrderVersionId,
      requestType = RequestType.VARIATION,
      versionNumber = 1,
    )
    newOrder.additionalDocuments.clear()
    newOrder.enforcementZoneConditions.clear()
    order.versions.add(newOrder.getCurrentVersion())
    newOrder.deviceWearer!!.firstName = order.deviceWearer!!.firstName + "Not"

    whenever(mockClient.getState("1")).thenReturn(CaseState.OPEN)

    val result = strategy.submitOrder(order, FmsOrderSource.CEMO)

    assertThat(result.success).isTrue
    assertThat(result.monitoringOrderResult.payload).contains("User entered:")
    assertThat(result.monitoringOrderResult.payload).contains("Change to address")
    assertThat(result.monitoringOrderResult.payload).contains("CEMO determined changes:")
    assertThat(result.monitoringOrderResult.payload).contains("Device wearer's name has changed")
  }

  @Test
  fun `Should find last successful submitted version and generate CEMO determined changes`() {
    val mockSubmissionResultId = UUID.randomUUID()
    val order = TestUtilities.createReadyToSubmitOrder(
      mockOrderId,
      mockOrderVersionId,
      requestType = RequestType.REQUEST,
      status = OrderStatus.SUBMITTED,
      fmsResultId = mockSubmissionResultId,
    )
    whenever(repo.getReferenceById(mockSubmissionResultId)).thenReturn(
      FmsSubmissionResult(
        id = mockSubmissionResultId,
        orderId = mockOrderId,
        strategy = FmsSubmissionStrategyKind.ORDER,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(
          payload = objectMapper.writeValueAsString(DeviceWearer.fromCemoOrder(order, mockFeatureFlags)),
          deviceWearerId = "1",
        ),
        monitoringOrderResult = FmsMonitoringOrderSubmissionResult(
          payload = objectMapper.writeValueAsString(MonitoringOrder.fromOrder(order, "1", mockFeatureFlags,FmsOrderSource.CEMO)),
        ),
      ),
    )

    val mockCancelledSubmissionResultId = UUID.randomUUID()
    val cancelledVariation = TestUtilities.createReadyToSubmitOrder(
      mockOrderId,
      mockOrderVersionId,
      requestType = RequestType.VARIATION,
      status = OrderStatus.SUBMITTED,
      fmsResultId = mockCancelledSubmissionResultId,
      versionNumber = 1,
    )
    cancelledVariation.deviceWearer!!.firstName = order.deviceWearer!!.firstName + "Not"
    order.versions.add(cancelledVariation.getCurrentVersion())

    whenever(repo.getReferenceById(mockCancelledSubmissionResultId)).thenReturn(
      FmsSubmissionResult(
        id = mockCancelledSubmissionResultId,
        orderId = mockOrderId,
        strategy = FmsSubmissionStrategyKind.VARIATION,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(
          deviceWearerId = "2",
        ),
      ),
    )

    val newOrder = TestUtilities.createReadyToSubmitOrder(
      mockOrderId,
      mockOrderVersionId,
      requestType = RequestType.VARIATION,
      versionNumber = 2,
    )
    newOrder.additionalDocuments.clear()
    newOrder.enforcementZoneConditions.clear()
    order.versions.add(newOrder.getCurrentVersion())
    newOrder.deviceWearer!!.firstName = order.deviceWearer!!.firstName + "Not" // should still identify as change

    whenever(mockClient.getState("1")).thenReturn(CaseState.OPEN)
    whenever(mockClient.getState("2")).thenReturn(CaseState.CANCELLED)

    val result = strategy.submitOrder(order, FmsOrderSource.CEMO)

    assertThat(result.success).isTrue
    assertThat(result.monitoringOrderResult.payload).contains("User entered:")
    assertThat(result.monitoringOrderResult.payload).contains("Change to address")
    assertThat(result.monitoringOrderResult.payload).contains("CEMO determined changes:")
    assertThat(result.monitoringOrderResult.payload).contains("Device wearer's name has changed")
  }
}
