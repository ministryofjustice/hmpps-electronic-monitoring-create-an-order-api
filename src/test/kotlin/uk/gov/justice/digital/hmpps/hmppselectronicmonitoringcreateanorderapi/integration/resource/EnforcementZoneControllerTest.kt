package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension.Companion.documentApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentUploadResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class EnforcementZoneControllerTest : IntegrationTestBase() {

  private val mockStartDate = ZonedDateTime.of(
    LocalDate.now(ZoneId.of("UTC")),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  ).plusMonths(1)
  private val mockEndDate = ZonedDateTime.of(
    LocalDate.now(ZoneId.of("UTC")),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  ).plusMonths(2)
  private val mockPastStartDate = ZonedDateTime.of(
    LocalDate.of(1970, 2, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )
  private val mockPastEndDate = mockPastStartDate.plusDays(1)
  private final val mockUser = "AUTH_ADM"

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Nested
  @DisplayName("PUT /api/orders/{orderId}/enforcementZone")
  inner class UpdateEnforcementZone {
    @Test
    fun `it should return an error if the order is not created by the user`() {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM_2"))
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = NOT_FOUND,
          developerMessage = "An editable order with ${order.id} does not exist",
          userMessage = "Not Found",
        ),
      )
    }

    @Test
    fun `it should return an error if the order is in a submitted state`() {
      val order = createSubmittedOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(),
          ),
        )
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = NOT_FOUND,
          developerMessage = "An editable order with ${order.id} does not exist",
          userMessage = "Not Found",
        ),
      )
    }

    @Test
    fun `it should return a validation error when request is not valid`() {
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
    fun `it should return a validation error when end date is in the past`() {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(
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
    fun `it should not return a validation error when start date is in the past`() {
      val order = createOrder()

      webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(
              startDate = mockPastStartDate,
            ),
          ),
        )
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk()
    }

    @Test
    fun `it should return a validation error when end date is before start date`() {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(
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
    fun `it should create 2 enforcement zone conditions`() {
      val order = createOrder()

      // Create first enforcement zone
      webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(
              zoneId = 0,
            ),
          ),
        )
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk

      // Create second enforcement zone
      webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(
              zoneId = 1,
            ),
          ),
        )
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk

      // Get updated order
      val updatedOrder = getOrder(order.id)

      // Verify order state matches expected state
      Assertions.assertThat(updatedOrder.enforcementZoneConditions).hasSize(2)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].startDate).isEqualTo(mockStartDate)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].endDate).isEqualTo(mockEndDate)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].description).isEqualTo("MockDescription")
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].duration).isEqualTo("MockDuration")
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].zoneId).isEqualTo(0)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![1].startDate).isEqualTo(mockStartDate)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![1].endDate).isEqualTo(mockEndDate)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![1].description).isEqualTo("MockDescription")
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![1].duration).isEqualTo("MockDuration")
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![1].zoneId).isEqualTo(1)
    }

    @Test
    fun `it should replace the enforcement zone condition`() {
      val order = createOrder()

      // Create first enforcement zone
      webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(
              duration = "ExistingDurationOld",
              description = "ExistingDescriptionOld",
            ),
          ),
        )
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk

      // Update enforcement zone
      webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(),
          ),
        )
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk

      // Get updated order
      val updatedOrder = getOrder(order.id)

      // Verify order state matches expected state
      Assertions.assertThat(updatedOrder.enforcementZoneConditions).hasSize(1)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].startDate).isEqualTo(mockStartDate)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].endDate).isEqualTo(mockEndDate)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].description).isEqualTo("MockDescription")
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].duration).isEqualTo("MockDuration")
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].zoneId).isEqualTo(0)
    }
  }

  @Nested
  @DisplayName("POST /api/orders/{orderId}/enforcementZone/{zoneId}/attachment")
  inner class UploadAttachment {
    @Suppress("ktlint:standard:max-line-length")
    private val validationMessage = "Validation failure: Unsupported or missing file type txt. Supported file types: pdf, jpeg, jpg"

    @Test
    fun `it should return a validation error for an invalid file extension`() {
      val order = createOrder()
      val bodyBuilder = createMultiPartBodyBuilder(mockFile("filename2.txt"))

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/enforcementZone/0/attachment")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = BAD_REQUEST,
          developerMessage = "Unsupported or missing file type txt. Supported file types: pdf, jpeg, jpg",
          userMessage = validationMessage,
        ),
      )
    }

    @Test
    fun `it should return an error if upload to document management api fails`() {
      val order = createOrder()
      val bodyBuilder = createMultiPartBodyBuilder(mockFile())

      // Return an error from the document api
      documentApi.stubUploadDocumentBadRequest(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "mock document api error",
          developerMessage = "",
        ),
      )

      // Create enforcement zone
      webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(
              duration = "ExistingDuration",
              description = "ExistingDescription",
              startDate = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1),
              endDate = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(2),
            ),
          ),
        )
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk

      // Upload attachment
      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/enforcementZone/0/attachment")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = BAD_REQUEST,
          developerMessage = "",
          userMessage = "mock document api error",
        ),
      )
    }

    @Test
    fun `it should replace the enforcement zone condition and delete file from document api`() {
      val order = createOrder()
      val bodyBuilder = createMultiPartBodyBuilder(mockFile())
      val bodyBuilder2 = createMultiPartBodyBuilder(mockFile("file-name-2.jpeg"))

      documentApi.stubDeleteDocument("(.*)")
      documentApi.stubUploadDocument(DocumentUploadResponse())

      // Create first enforcement zone
      webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(
              duration = "ExistingDuration",
              description = "ExistingDescription",
            ),
          ),
        )
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk

      // Upload first attachment
      webTestClient.post()
        .uri("/api/orders/${order.id}/enforcementZone/0/attachment")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk

      // Update enforcement zone
      webTestClient.put()
        .uri("/api/orders/${order.id}/enforcementZone")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRequestBody(),
          ),
        )
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk

      // Upload second attachment
      webTestClient.post()
        .uri("/api/orders/${order.id}/enforcementZone/0/attachment")
        .bodyValue(bodyBuilder2.build())
        .headers(setAuthorisation(mockUser))
        .exchange()
        .expectStatus()
        .isOk

      // Get updated order
      val updatedOrder = getOrder(order.id)

      // Verify order state matches expected state
      Assertions.assertThat(updatedOrder.enforcementZoneConditions).hasSize(1)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].startDate).isEqualTo(mockStartDate)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].endDate).isEqualTo(mockEndDate)
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].description).isEqualTo("MockDescription")
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].duration).isEqualTo("MockDuration")
      Assertions.assertThat(updatedOrder.enforcementZoneConditions!![0].zoneId).isEqualTo(0)

      // Verify 2 document were uploaded to the document api
      documentApi.verify(2, postRequestedFor(urlMatching("/documents/CEMO_ATTACHMENT/(.*)")))

      // Verify 1 document was deleted from the document api
      documentApi.verify(1, deleteRequestedFor(urlMatching("/documents/(.*)")))
    }
  }

  fun mockRequestBody(
    startDate: ZonedDateTime? = mockStartDate,
    endDate: ZonedDateTime? = mockEndDate,
    description: String? = "MockDescription",
    duration: String? = "MockDuration",
    zoneId: Int? = 0,
  ): String {
    return """
      {
        "zoneType": "EXCLUSION",
        "startDate": "$startDate",
        "endDate": "$endDate",
        "description": "$description",
        "duration": "$duration",
        "zoneId": $zoneId
      }
    """.trimIndent()
  }
}
