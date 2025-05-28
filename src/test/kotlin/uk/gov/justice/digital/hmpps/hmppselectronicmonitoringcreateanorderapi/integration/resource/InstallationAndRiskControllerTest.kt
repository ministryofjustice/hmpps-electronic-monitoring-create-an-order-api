package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationAndRiskDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

class InstallationAndRiskControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Nested
  @DisplayName("PUT /api/orders/{orderId}/installation-and-risk")
  inner class UpdateInstallationAndRisk {
    @Test
    fun `it should return an error if the order was not created by the user`() {
      val order = createOrder()
      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/installation-and-risk")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockValidRequestBody(),
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
    fun `it should return not found if the order does not exist`() {
      webTestClient.put()
        .uri("/api/orders/${UUID.randomUUID()}/installation-and-risk")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockValidRequestBody(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `it should return an error if the order is in a submitted state`() {
      val order = createSubmittedOrder()
      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/installation-and-risk")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockValidRequestBody(),
          ),
        )
        .headers(setAuthorisation())
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

    @ParameterizedTest(name = "it should update installation and risk with valid request body with riskCategory {0}")
    @ValueSource(
      strings = [
        "SEXUAL_OFFENCES",
        "SAFEGUARDING_ISSUE",
        "SAFEGUARDING_ADULT",
        "SAFEGUARDING_CHILD",
        "SAFEGUARDING_DOMESTIC_ABUSE",
      ],
    )
    fun `it should update installation and risk with valid request body`(riskCategory: String) {
      val order = createOrder()
      val mockRisk = mockValidRequestBody(
        offence = "VIOLENCE_AGAINST_THE_PERSON",
        riskCategory = arrayOf(riskCategory),
        riskDetails = "mockRisk",
        mappaLevel = "mockMappaLevel",
        mappaCaseType = "mockMappaType",
      )
      webTestClient.put()
        .uri("/api/orders/${order.id}/installation-and-risk")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRisk,
          ),
        )
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk

      // Get updated order
      val updatedOrder = getOrder(order.id)

      Assertions.assertThat(updatedOrder.installationAndRisk?.offence).isEqualTo("VIOLENCE_AGAINST_THE_PERSON")
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskCategory?.first()).isEqualTo(riskCategory)
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskDetails).isEqualTo("mockRisk")
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaLevel).isEqualTo("mockMappaLevel")
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaCaseType).isEqualTo("mockMappaType")
    }

    @Test
    fun `it should update installation and risk with default values`() {
      val order = createOrder()
      val mockRisk = mockValidRequestBody()

      webTestClient.put()
        .uri("/api/orders/${order.id}/installation-and-risk")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRisk,
          ),
        )
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk

      // Get updated order
      val updatedOrder = getOrder(order.id)

      Assertions.assertThat(updatedOrder.installationAndRisk?.offence).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskCategory).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskDetails).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaLevel).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaCaseType).isNull()
    }

    @Test
    fun `it should update installation and risk with offence set to an empty string`() {
      val order = createOrder()
      val mockRisk = mockValidRequestBody(
        offence = "",
      )

      webTestClient.put()
        .uri("/api/orders/${order.id}/installation-and-risk")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            mockRisk,
          ),
        )
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk

      // Get updated order
      val updatedOrder = getOrder(order.id)

      Assertions.assertThat(updatedOrder.installationAndRisk?.offence).isEqualTo("")
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskCategory).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskDetails).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaLevel).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaCaseType).isNull()
    }

    @Test
    fun `it should return an error if an invalid data is submitted`() {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/installation-and-risk")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {
                "offence": "INVALID",
                "riskCategory": ["INVALID"],
                "riskDetails": "",
                "mappaLevel": "",
                "mappaCaseType": ""
              }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ValidationError::class.java)
        .returnResult()
        .responseBody

      Assertions.assertThat(result).isNotNull
      Assertions.assertThat(result).hasSize(2)
      Assertions.assertThat(result).contains(
        ValidationError(
          "riskCategory",
          ValidationErrors.InstallationAndRisk.RISK_CATEGORY_VALID,
        ),
      )
      Assertions.assertThat(result).contains(
        ValidationError(
          "offence",
          ValidationErrors.InstallationAndRisk.OFFENCE_VALID,
        ),
      )
    }

    fun mockValidRequestBody(
      offence: String? = null,
      riskCategory: Array<String>? = null,
      riskDetails: String? = null,
      mappaLevel: String? = null,
      mappaCaseType: String? = null,
    ): String {
      val condition = UpdateInstallationAndRiskDto(
        offence = offence,
        riskCategory = riskCategory,
        riskDetails = riskDetails,
        mappaLevel = mappaLevel,
        mappaCaseType = mappaCaseType,
      )
      return objectMapper.writeValueAsString(condition)
    }
  }
}
