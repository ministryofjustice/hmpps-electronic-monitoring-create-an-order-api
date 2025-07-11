package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.ZonedDateTime
import java.util.*

class VariationControllerTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Nested
  @DisplayName("POST /api/orders/{orderId}/variation")
  inner class PostVariation {

    @Test
    fun `it should not be possible to update the variation details if the variation is owned by another user`() {
      val variation = createVariation()

      webTestClient.put()
        .uri("/api/orders/${variation.id}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "variationType": "CHANGE_TO_ADDRESS",
                "variationDate": "2024-01-01T00:00:00.000Z",
                "variationDescription": "Change to address"
              }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM_2"))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `it should not be possible to update the variation details if the variation does not exist`() {
      webTestClient.put()
        .uri("/api/orders/${UUID.randomUUID()}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "variationType": "CHANGE_TO_ADDRESS",
                "variationDate": "2024-01-01T00:00:00.000Z",
                "variationDescription": "Change to address"
              }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `it should not be possible to update the variation details if the variation has been submitted`() {
      val variation = createSubmittedVariation()

      webTestClient.put()
        .uri("/api/orders/${variation.id}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "variationType": "CHANGE_TO_ADDRESS",
                "variationDate": "2024-01-01T00:00:00.000Z",
                "variationDescription": "Change to address"                
              }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `it should not be possible to update the variation details if the mandatory fields are missing`() {
      val variation = createVariation()

      val result = webTestClient.put()
        .uri("/api/orders/${variation.id}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {}
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ValidationError::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody).isNotNull
      Assertions.assertThat(result.responseBody).hasSize(3)
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("variationType", "Select what you have changed"),
      )
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("variationDate", "Variation date is required"),
      )
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("variationDescription", "Enter information on what you have changed"),
      )
    }

    @Test
    fun `it should not be possible to update the variation details with an invalid variationType`() {
      val variation = createVariation()

      val result = webTestClient.put()
        .uri("/api/orders/${variation.id}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "variationType": "UNKNOWN",
                "variationDate": "2024-01-01T00:00:00.000Z",
                "variationDescription": "Change to address"
              }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ValidationError::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody).isNotNull
      Assertions.assertThat(result.responseBody).hasSize(1)
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("variationType", "Variation type must be a valid variation type"),
      )
    }

    @Test
    fun `it should not be possible to update the variation details with an invalid variationDate`() {
      val variation = createVariation()

      val result = webTestClient.put()
        .uri("/api/orders/${variation.id}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "variationType": "CHANGE_TO_ADDRESS",
                "variationDate": "2024-02-31T00:00:00.000Z",
                "variationDescription": "Change to address"                
              }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ValidationError::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody).isNotNull
      Assertions.assertThat(result.responseBody).hasSize(1)
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("variationDate", "Variation date must be a valid date"),
      )
    }

    @Test
    fun `it should not be possible to update the variation details with an invalid variationDescription`() {
      val variation = createVariation()

      val result = webTestClient.put()
        .uri("/api/orders/${variation.id}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "variationType": "CHANGE_TO_ADDRESS",
                "variationDate": "2024-01-01T00:00:00.000Z",
                "variationDescription": ""                
              }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ValidationError::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody).isNotNull
      Assertions.assertThat(result.responseBody).hasSize(1)
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("variationDescription", "Enter information on what you have changed"),
      )
    }

    @ParameterizedTest(name = "it should not be possible to update the variation details with DDv4 variationType = {0}")
    @ValueSource(strings = ["CURFEW_HOURS", "ADDRESS", "ENFORCEMENT_ADD", "ENFORCEMENT_UPDATE", "SUSPENSION"])
    fun `it should not be possible to update the variation details with DDv4 variation types`(variationType: String) {
      val variation =
        createStoredOrder(type = RequestType.VARIATION, dataDictionaryVersion = DataDictionaryVersion.DDV5)

      val result = webTestClient.put()
        .uri("/api/orders/${variation.id}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "variationType": "$variationType",
                "variationDate": "2024-01-01T00:00:00.000Z",
                "variationDescription": "Change to address"                
              }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody).isNotNull
      Assertions.assertThat(result.responseBody).hasSize(1)
      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = BAD_REQUEST,
          developerMessage = "Variation type $variationType is obsolete",
          userMessage = "Validation failure: Variation type $variationType is obsolete",
        ),
      )
    }

    @ParameterizedTest(name = "it should  be possible to update the variation details with DDv5 variationType = {0}")
    @ValueSource(
      strings = [
        "CHANGE_TO_ADDRESS",
        "CHANGE_TO_PERSONAL_DETAILS",
        "CHANGE_TO_ADD_AN_EXCLUSION_ZONES",
        "CHANGE_TO_AN_EXISTING_EXCLUSION",
        "CHANGE_TO_CURFEW_HOURS",
        "ORDER_SUSPENSION",
        "CHANGE_TO_DEVICE_TYPE",
        "CHANGE_TO_ENFORCEABLE_CONDITION",
        "ADMIN_ERROR",
        "OTHER",
      ],
    )
    fun `it should be possible to update the variation details with all variation types`(variationType: String) {
      val variation = createVariation()

      val result = webTestClient.put()
        .uri("/api/orders/${variation.id}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "variationType": "$variationType",
                "variationDate": "2024-01-01T00:00:00.000Z",
                "variationDescription": "Change to $variationType"
              }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(VariationDetails::class.java)
        .returnResult()
        .responseBody!!

      Assertions.assertThat(result.variationType).isEqualTo(VariationType.valueOf(variationType))
      Assertions.assertThat(result.variationDate).isEqualTo(ZonedDateTime.parse("2024-01-01T00:00:00.000Z"))
      Assertions.assertThat(result.variationDescription).isEqualTo("Change to $variationType")
    }
  }
}
