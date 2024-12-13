package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.time.ZonedDateTime
import java.util.*

class VariationControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var repo: OrderRepository

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
                "variationType": "ADDRESS",
                "variationDate": "2024-01-01T00:00:00.000Z"
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
                "variationType": "ADDRESS",
                "variationDate": "2024-01-01T00:00:00.000Z"
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
      val variation = createVariation()

      variation.status = OrderStatus.SUBMITTED
      repo.save(variation)

      webTestClient.put()
        .uri("/api/orders/${variation.id}/variation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "variationType": "ADDRESS",
                "variationDate": "2024-01-01T00:00:00.000Z"
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
      Assertions.assertThat(result.responseBody).hasSize(2)
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("variationType", "Variation type is required"),
      )
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("variationDate", "Variation date is required"),
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
                "variationDate": "2024-01-01T00:00:00.000Z"
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
                "variationType": "ADDRESS",
                "variationDate": "2024-02-31T00:00:00.000Z"
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

    @ParameterizedTest(name = "it should be possible to update the variation details with variationType = {0}")
    @ValueSource(strings = ["CURFEW_HOURS", "ADDRESS", "ENFORCEMENT_ADD", "ENFORCEMENT_UPDATE", "SUSPENSION"])
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
                "variationDate": "2024-01-01T00:00:00.000Z"
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

      Assertions.assertThat(result.orderId).isEqualTo(variation.id)
      Assertions.assertThat(result.variationType).isEqualTo(VariationType.valueOf(variationType))
      Assertions.assertThat(result.variationDate).isEqualTo(ZonedDateTime.parse("2024-01-01T00:00:00.000Z"))
    }
  }
}
