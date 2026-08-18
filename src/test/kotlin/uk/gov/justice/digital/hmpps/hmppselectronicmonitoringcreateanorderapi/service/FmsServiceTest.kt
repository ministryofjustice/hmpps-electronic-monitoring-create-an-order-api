package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import DeviceWearerPayloadVersion
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRetrieveDWandMO
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

@ActiveProfiles("test")
@JsonTest
class FmsServiceTest {

  private lateinit var service: FmsService
  private lateinit var mockClient: FmsClient
  private lateinit var mockDocumentApiClient: DocumentApiClient
  private lateinit var objectMapper: ObjectMapper
  private lateinit var repo: FmsSubmissionResultRepository
  private lateinit var eventService: EventService
  private val mockFeatureFlags =
    FeatureFlags(
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
      ddV6CourtMappings = false,
      deviceWearerPayloadVersion = DeviceWearerPayloadVersion.Prod,
    )
  private lateinit var env: Environment

  @BeforeEach
  fun setUp() {
    mockClient = mock(FmsClient::class.java)
    mockDocumentApiClient = mock(DocumentApiClient::class.java)
    objectMapper = JsonMapper.builder()
      .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
      .build()
    repo = mock(FmsSubmissionResultRepository::class.java)
    env = mock(Environment::class.java)
    eventService = mock(EventService::class.java)
    service =
      FmsService(
        mockClient,
        mockDocumentApiClient,
        objectMapper,
        repo,
        true,
        true,
        mockFeatureFlags,
        eventService,
      )
  }

  @Test
  fun `Should return device wearer result payload`() {
    val mockId = UUID.randomUUID()
    val mockResult = FmsSubmissionResult(
      mockId,
      orderId = mockId,
      strategy = FmsSubmissionStrategyKind.DUMMY,
      submissionDate = OffsetDateTime.now(),
      deviceWearerResult = FmsDeviceWearerSubmissionResult(payload = "mockPayload"),
      orderSource = FmsOrderSource.CEMO,
    )
    whenever(repo.findById(mockId)).thenReturn(Optional.of(mockResult))

    val result = service.getFmsDeviceWearerSubmissionResultById(mockId)
    assertThat(result).isEqualTo("mockPayload")
  }

  @Test
  fun `Should return monitoring order result payload`() {
    val mockId = UUID.randomUUID()
    val mockResult = FmsSubmissionResult(
      mockId,
      orderId = mockId,
      strategy = FmsSubmissionStrategyKind.DUMMY,
      submissionDate = OffsetDateTime.now(),
      monitoringOrderResult = FmsMonitoringOrderSubmissionResult(payload = "mockPayload"),
      deviceWearerResult = FmsDeviceWearerSubmissionResult(payload = "mockDWPayload"),
      orderSource = FmsOrderSource.CEMO,
    )
    whenever(repo.findById(mockId)).thenReturn(Optional.of(mockResult))

    val result = service.getFmsMonitoringOrderSubmissionResultByOrderId(mockId)
    assertThat(result).isEqualTo("mockPayload")
  }

  @Test
  fun `should map dev device wearer fields in dev`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder()

    val mockFmsResponse = FmsResponse(result = listOf(FmsResult(id = mockOrder.id.toString())))
    whenever(
      mockClient.createDeviceWearer(
        any(),
        eq(mockOrder.id),
        eq(FmsOrderSource.CEMO),
      ),
    ).thenReturn(mockFmsResponse)
    whenever(mockClient.createMonitoringOrder(any(), eq(mockOrder.id), eq(FmsOrderSource.CEMO))).thenReturn(
      mockFmsResponse,
    )

    service = FmsService(
      mockClient,
      mockDocumentApiClient,
      objectMapper,
      repo,
      true,
      true,
      FeatureFlags(
        dataDictionaryVersion = DataDictionaryVersion.DDV6,
        ddV6CourtMappings = false,
        deviceWearerPayloadVersion = DeviceWearerPayloadVersion.Dev,
      ),
      eventService,
    )

    service.submitOrder(mockOrder, FmsOrderSource.CEMO)

    val payloadCaptor = argumentCaptor<String>()
    val orderIdCaptor = argumentCaptor<UUID>()

    verify(mockClient).createDeviceWearer(payloadCaptor.capture(), orderIdCaptor.capture(), eq(FmsOrderSource.CEMO))

    assertThat(orderIdCaptor.firstValue).isEqualTo(mockOrder.id)

    val capturedJsonString = payloadCaptor.firstValue
    val jsonNode = objectMapper.readTree(capturedJsonString)

    print(jsonNode.toString())
    assertThat(jsonNode.has("mappa_case_type")).isTrue

    assertThat(jsonNode.has("mappa_category")).isTrue
  }

  @Nested
  @DisplayName("getLatestOrderVersion")
  inner class GetLatestOrderVersion {
    @Test
    fun `should return null when original case id is null`() {
      val mockOrder = TestUtilities.createReadyToSubmitOrder()
      val mockFmsResultId = UUID.randomUUID()
      mockOrder.fmsResultId = mockFmsResultId
      val mockFmsResponse = FmsSubmissionResult(
        id = mockFmsResultId,
        orderId = mockOrder.id,
        submissionDate = OffsetDateTime.now(),
        strategy = FmsSubmissionStrategyKind.ORDER,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(),
        monitoringOrderResult = FmsMonitoringOrderSubmissionResult(),
      )
      whenever(repo.getReferenceById(mockFmsResultId)).thenReturn(mockFmsResponse)
      val result = service.getLatestOrderVersion(mockOrder)
      assertThat(result).isNull()
      verifyNoInteractions(mockClient)
      verify(eventService, never()).recordEvent(any(), any(), any())
    }

    @Test
    fun `should return latest order version when FMS details are retrieved`() {
      val mockOrder = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.SUBMITTED)
      val mockCaseId = "MockCaseId"
      val mockFmsResultId = UUID.randomUUID()
      mockOrder.fmsResultId = mockFmsResultId
      val mockFmsResponse = FmsSubmissionResult(
        id = mockFmsResultId,
        orderId = mockOrder.id,
        submissionDate = OffsetDateTime.now(),
        strategy = FmsSubmissionStrategyKind.ORDER,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = mockCaseId),
        monitoringOrderResult = FmsMonitoringOrderSubmissionResult(),
      )
      whenever(repo.getReferenceById(mockFmsResultId)).thenReturn(mockFmsResponse)
      whenever(mockClient.getLastestOrderDetails(mockCaseId)).thenReturn(
        FmsRetrieveDWandMO(
          caseId = "CASE123",
          deviceWearer = DeviceWearer(firstName = "John", lastName = "Smith", dateOfBirth = "1991-01-01"),
          monitoringOrder = MonitoringOrder(),
        ),
      )
      val result = service.getLatestOrderVersion(mockOrder)
      assertThat(result?.deviceWearer?.firstName).isEqualTo("John")
      assertThat(result?.deviceWearer?.lastName).isEqualTo("Smith")
      verify(eventService, never()).recordEvent(any(), any(), any())
    }

    @Test
    fun `should record event and return null when FMS retrieval fails`() {
      val mockOrder = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.SUBMITTED)
      val mockCaseId = "MockCaseId"
      val errorMessage = "FMS unavailable"
      val mockFmsResultId = UUID.randomUUID()
      mockOrder.fmsResultId = mockFmsResultId
      val mockFmsResponse = FmsSubmissionResult(
        id = mockFmsResultId,
        orderId = mockOrder.id,
        submissionDate = OffsetDateTime.now(),
        strategy = FmsSubmissionStrategyKind.ORDER,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = mockCaseId),
        monitoringOrderResult = FmsMonitoringOrderSubmissionResult(),
      )
      whenever(repo.getReferenceById(mockFmsResultId)).thenReturn(mockFmsResponse)
      whenever(mockClient.getLastestOrderDetails(mockCaseId)).thenThrow(CreateSercoEntityException(errorMessage))
      val result = service.getLatestOrderVersion(mockOrder)
      verify(eventService).recordEvent(
        eq("Failed to retrieve latest order from FMS: $mockCaseId"),
        eq(
          mapOf(
            "error" to errorMessage,
            "caseId" to mockCaseId,
          ),
        ),
        any(),
      )
    }
  }
}
