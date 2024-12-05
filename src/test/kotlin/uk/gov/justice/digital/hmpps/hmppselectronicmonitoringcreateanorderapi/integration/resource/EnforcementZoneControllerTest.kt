package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.internal.verification.Times
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension.Companion.documentApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentUploadResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.EnforcementZoneRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class EnforcementZoneControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var orderRepo: OrderRepository

  @SpyBean
  lateinit var repo: EnforcementZoneRepository

  @SpyBean
  lateinit var apiCLient: DocumentApiClient

  private val mockStartDate = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(1)
  private val mockEndDate = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(2)
  private val mockPastStartDate = ZonedDateTime.of(
    LocalDate.of(1970, 2, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )
  private val mockPastEndDate = mockPastStartDate.plusDays(1)
  private final val mockUser = "AUTH_ADM"
  private final val mockOrder = Order(username = mockUser, status = OrderStatus.IN_PROGRESS, type = OrderType.REQUEST)

  @BeforeEach
  fun setup() {
    orderRepo.deleteAll()
    repo.deleteAll()

    orderRepo.save(mockOrder)
  }

  @Test
  fun `Enforcement zone for an order created by a different user are not update-able`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/enforcementZone")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()
    val error = result.responseBody!!.first()
    Assertions.assertThat(
      error.developerMessage,
    ).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Enforcement zone for an order already submitted are not update-able`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/enforcementZone")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()
    val error = result.responseBody!!.first()
    Assertions.assertThat(
      error.developerMessage,
    ).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Should return error when enforcement zone is not valid`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/enforcementZone")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderId": "${order.id}",
              "zoneType": null,
              "startDate": null,
              "endDate": null,
              "description": null,
              "duration": null,
              "zoneId": null
            }
          """.trimIndent(),

        ),
      )
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(4)

    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("zoneType", "Enforcement zone type is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("startDate", "Enforcement zone start date is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("description", "Enforcement zone description is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("duration", "Enforcement zone duration is required"),
    )
  }

  @Test
  fun `Should return error when enforcement zone end date is in the past`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/enforcementZone")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            order.id,
            startDate = mockPastStartDate,
            endDate = mockPastEndDate,
          ),
        ),
      )
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(1)

    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("endDate", "Enforcement zone end date must be in the future"),
    )
  }

  @Test
  fun `Should not return error when enforcement zone start date is in the past`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/enforcementZone")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            order.id,
            startDate = mockPastStartDate,
          ),
        ),
      )
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(EnforcementZoneConditions::class.java)
  }

  @Test
  fun `Should return error when enforcement zone end date is not after start date`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/enforcementZone")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            order.id,
            startDate = mockStartDate,
            endDate = mockStartDate.minusDays(1),
          ),
        ),
      )
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(1)

    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("endDate", "End date must be after start date"),
    )
  }

  @Test
  fun `Should replace the enforcement zone condition and clear file in document api`() {
    val zone = EnforcementZoneConditions(
      zoneId = 0,
      zoneType = EnforcementZoneType.EXCLUSION,
      duration = "ExistingDuration",
      orderId = mockOrder.id,
      description = "ExistingDescription",
      startDate = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1),
      endDate = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(2),
      fileId = UUID.randomUUID(),
    )
    mockOrder.enforcementZoneConditions.add(zone)
    orderRepo.save(mockOrder)
    documentApi.stubDeleteDocument(zone.fileId.toString())

    webTestClient.put()
      .uri("/api/orders/${mockOrder.id}/enforcementZone")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(mockOrder.id),
        ),
      )
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isOk
    verify(repo, Times(1)).deleteById(zone.id)

    argumentCaptor<EnforcementZoneConditions>().apply {
      verify(repo, Times(1)).save(capture())
      Assertions.assertThat(firstValue.orderId).isEqualTo(mockOrder.id)
      Assertions.assertThat(firstValue.startDate).isEqualTo(mockStartDate)
      Assertions.assertThat(firstValue.endDate).isEqualTo(mockEndDate)
      Assertions.assertThat(firstValue.description).isEqualTo("MockDescription")
      Assertions.assertThat(firstValue.duration).isEqualTo("MockDuration")
      Assertions.assertThat(firstValue.zoneId).isEqualTo(0)
    }

    verify(apiCLient, Times(1)).deleteDocument(zone.fileId.toString())
  }

  @Test
  fun `file extension not allow return bad request with validation error`() {
    val bodyBuilder = MultipartBodyBuilder()
    bodyBuilder.part("file", ByteArrayResource(mockFile("filename2.txt").bytes))
      .header("Content-Disposition", "form-data; name=file; filename=filename2.txt")

    val result = webTestClient.post()
      .uri("/api/orders/${mockOrder.id}/enforcementZone/0/attachment")
      .bodyValue(bodyBuilder.build())
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()

    val error = result.responseBody!!.first()
    Assertions.assertThat(
      error.userMessage,
    ).isEqualTo(
      "Validation failure: Unsupported or missing file type txt. Supported file types: pdf, jpeg, jpg",
    )
  }

  @Test
  fun `Return error when Document management api validation failed,`() {
    val zone = EnforcementZoneConditions(
      zoneId = 0,
      zoneType = EnforcementZoneType.EXCLUSION,
      duration = "ExistingDuration",
      orderId = mockOrder.id,
      description = "ExistingDescription",
      startDate = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1),
      endDate = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(2),

    )
    mockOrder.enforcementZoneConditions.add(zone)
    orderRepo.save(mockOrder)
    val bodyBuilder = MultipartBodyBuilder()
    bodyBuilder.part("file", ByteArrayResource(mockFile().bytes))
      .header("Content-Disposition", "form-data; name=file; filename=file-name.jpeg")
    documentApi.stubUploadDocumentBadRequest(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "mock document api error",
        developerMessage = "",
      ),
    )

    val result = webTestClient.post()
      .uri("/api/orders/${mockOrder.id}/enforcementZone/0/attachment")
      .bodyValue(bodyBuilder.build())
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()

    val error = result.responseBody!!.first()
    Assertions.assertThat(error.userMessage)
      .isEqualTo("mock document api error")
  }

  @Test
  fun `Should replace the document and update enforcement zone with file id and name`() {
    val zone = EnforcementZoneConditions(
      zoneId = 0,
      zoneType = EnforcementZoneType.EXCLUSION,
      duration = "ExistingDuration",
      orderId = mockOrder.id,
      description = "ExistingDescription",
      startDate = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1),
      endDate = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(2),
      fileId = UUID.randomUUID(),
    )
    mockOrder.enforcementZoneConditions.add(zone)
    orderRepo.save(mockOrder)

    val bodyBuilder = MultipartBodyBuilder()
    bodyBuilder.part("file", ByteArrayResource(mockFile().bytes))
      .header("Content-Disposition", "form-data; name=file; filename=filename2.jpeg")

    documentApi.stubDeleteDocument(zone.fileId.toString())
    documentApi.stubUploadDocument(DocumentUploadResponse())
    webTestClient.post()
      .uri("/api/orders/${mockOrder.id}/enforcementZone/0/attachment")
      .bodyValue(bodyBuilder.build())
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isOk

    verify(apiCLient, Times(1)).deleteDocument(zone.fileId.toString())

    argumentCaptor<EnforcementZoneConditions>().apply {
      verify(repo, Times(1)).save(capture())

      Assertions.assertThat(firstValue.fileName).isEqualTo("filename2.jpeg")
    }
  }

  fun mockRequestBody(
    orderId: UUID,
    startDate: ZonedDateTime? = mockStartDate,
    endDate: ZonedDateTime? = mockEndDate,
    description: String? = "MockDescription",
    duration: String? = "MockDuration",
    zoneId: Int? = 0,
  ): String {
    return """
            {
              "orderId": "$orderId",
              "zoneType": "EXCLUSION",
              "startDate": "$startDate",
              "endDate": "$endDate",
              "description": "$description",
              "duration": "$duration",
              "zoneId": $zoneId
            }
    """.trimIndent()
  }

  fun mockFile(fileName: String? = "file-name.jpeg"): MockMultipartFile {
    return MockMultipartFile(
      "file",
      "file-name.jpeg",
      MediaType.IMAGE_JPEG_VALUE,
      "Test file content".toByteArray(),
    )
  }
}
