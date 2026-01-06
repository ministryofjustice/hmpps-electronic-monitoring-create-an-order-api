package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
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

  @BeforeEach
  fun setUp() {
    mockClient = mock(FmsClient::class.java)
    mockDocumentApiClient = mock(DocumentApiClient::class.java)
    objectMapper = ObjectMapper()
    repo = mock(FmsSubmissionResultRepository::class.java)
    service = FmsService(mockClient, mockDocumentApiClient, objectMapper, repo, true, true)
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
}
