package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UpdateOrderIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.UpdateDetailsOfInstallationDto

class DetailsOfInstallationControllerTest : UpdateOrderIntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  override val uri = "/api/orders/:orderId/details-of-installation"
  override fun createValidBody(): String = mockValidRequestBody()

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `should update details of installation`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/details-of-installation")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(
            riskCategory = arrayOf("THREATS_OF_VIOLENCE", "SEXUAL_OFFENCES"),
            riskDetails = "some details",
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange().expectStatus().isOk

    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.detailsOfInstallation).isNotNull
    Assertions.assertThat(updatedOrder.detailsOfInstallation?.riskCategory)
      .isEqualTo(arrayOf("THREATS_OF_VIOLENCE", "SEXUAL_OFFENCES"))
    Assertions.assertThat(updatedOrder.detailsOfInstallation?.riskDetails).isEqualTo("some details")
  }

  @Test
  fun `should throw error if risk category is invalid`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/details-of-installation")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(
            riskCategory = arrayOf("some invalid category"),
            riskDetails = "",
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange().expectStatus().isBadRequest().expectBodyList(ValidationError::class.java).returnResult()

    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("riskCategory", "Risk categories must be a valid risk category"),
    )
  }

  private fun mockValidRequestBody(riskCategory: Array<String>? = arrayOf(), riskDetails: String? = null): String {
    val dto = UpdateDetailsOfInstallationDto(riskCategory = riskCategory, riskDetails = riskDetails)

    return objectMapper.writeValueAsString(dto)
  }
}
