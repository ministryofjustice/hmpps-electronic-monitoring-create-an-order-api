package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import DeviceWearerPayloadVersion
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
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
    objectMapper = ObjectMapper()
    repo = mock(FmsSubmissionResultRepository::class.java)
    env = mock(Environment::class.java)
    service =
      FmsService(mockClient, mockDocumentApiClient, objectMapper, repo, true, true, mockFeatureFlags)
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

//  @Test
//  fun `should not map dev devicer wearer fields in prod`() {
//    val mockOrder = TestUtilities.createReadyToSubmitOrder()
//
//    val mockFmsResponse = FmsResponse(result = listOf(FmsResult(id = mockOrder.id.toString())))
//    whenever(mockClient.createDeviceWearer(any(), eq(mockOrder.id))).thenReturn(mockFmsResponse)
//    whenever(mockClient.createMonitoringOrder(any(), eq(mockOrder.id))).thenReturn(mockFmsResponse)
//
//    service = FmsService(
//      mockClient,
//      mockDocumentApiClient,
//      objectMapper,
//      repo,
//      true,
//      true,
//      mockFeatureFlags,
//    )
//
//    service.submitOrder(mockOrder, FmsOrderSource.CEMO)
//
//    val payloadCaptor = argumentCaptor<String>()
//    val orderIdCaptor = argumentCaptor<UUID>()
//
//    verify(mockClient).createDeviceWearer(payloadCaptor.capture(), orderIdCaptor.capture())
//
//    assertThat(orderIdCaptor.firstValue).isEqualTo(mockOrder.id)
//
//    val capturedJsonString = payloadCaptor.firstValue
//    val jsonNode = objectMapper.readTree(capturedJsonString)
//
//    print(jsonNode.toString())
//    assertThat(jsonNode.has("mappaCaseType")).isTrue
//
//    assertThat(jsonNode.has("mappaCategory")).isFalse
//  }

  @Test
  fun `should map dev devicer wearer fields in dev`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder()

    val mockFmsResponse = FmsResponse(result = listOf(FmsResult(id = mockOrder.id.toString())))
    whenever(mockClient.createDeviceWearer(any(), eq(mockOrder.id))).thenReturn(mockFmsResponse)
    whenever(mockClient.createMonitoringOrder(any(), eq(mockOrder.id))).thenReturn(mockFmsResponse)

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
    )

    service.submitOrder(mockOrder, FmsOrderSource.CEMO)

    val payloadCaptor = argumentCaptor<String>()
    val orderIdCaptor = argumentCaptor<UUID>()

    verify(mockClient).createDeviceWearer(payloadCaptor.capture(), orderIdCaptor.capture())

    assertThat(orderIdCaptor.firstValue).isEqualTo(mockOrder.id)

    val capturedJsonString = payloadCaptor.firstValue
    val jsonNode = objectMapper.readTree(capturedJsonString)

    print(jsonNode.toString())
    assertThat(jsonNode.has("mappaCaseType")).isTrue

    assertThat(jsonNode.has("mappaCategory")).isTrue
  }
}
