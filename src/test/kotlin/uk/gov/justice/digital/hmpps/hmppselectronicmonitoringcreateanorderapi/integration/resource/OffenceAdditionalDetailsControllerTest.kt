package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UpdateOrderIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UriTestCase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceAdditionalDetailsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.UUID

class OffenceAdditionalDetailsControllerTest : UpdateOrderIntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  override val testUris: List<UriTestCase> = listOf(
    UriTestCase(uri = "/api/orders/:orderId/offence-additional-details", createValidBody = {
      mockValidRequestBody(additionalDetails = "mock")
    }, httpMethod = HttpMethod.PUT),
  )

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `should add offence additional details`() {
    val order = createOrder()
    val details = "mock additional information about offence"

    callOffenceAdditionalDetailsEndpoint(
      order.id,
      mockValidRequestBody(additionalDetails = details),
    ).expectStatus().isOk

    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.offenceAdditionalDetails).isNotNull
    Assertions.assertThat(updatedOrder.offenceAdditionalDetails?.additionalDetails).isEqualTo(details)
  }

  @Test
  fun `should update offence additional details`() {
    val order = createOrder()
    val details = "mock additional information about offence"

    callOffenceAdditionalDetailsEndpoint(
      order.id,
      mockValidRequestBody(additionalDetails = details),
    ).expectStatus().isOk

    val updatedDetails = "updated offence information"
    callOffenceAdditionalDetailsEndpoint(
      order.id,
      mockValidRequestBody(additionalDetails = updatedDetails),
    ).expectStatus().isOk

    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.offenceAdditionalDetails?.additionalDetails).isEqualTo(updatedDetails)
  }

  @Test
  fun `should return error when offence additional details are too long`() {
    val order = createOrder()
    val longDetails = "a".repeat(501)

    val result = callOffenceAdditionalDetailsEndpoint(
      order.id,
      mockValidRequestBody(additionalDetails = longDetails),
    ).expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    Assertions.assertThat(
      result.responseBody!!,
    ).contains(ValidationError("additionalDetails", "Offence Additional details is too long"))
  }

  @Test
  fun `should return error when Yes is selected without details provided`() {
    val order = createOrder()

    val result = callOffenceAdditionalDetailsEndpoint(
      order.id,
      mockValidRequestBody(additionalDetailsRequired = true, additionalDetails = ""),
    ).expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    Assertions.assertThat(
      result.responseBody!!,
    ).contains(ValidationError("additionalDetails", ValidationErrors.OffenceAdditionalDetails.DETAILS_REQUIRED))
  }

  @Test
  fun `should succeed when No is selected`() {
    val order = createOrder()

    val result = callOffenceAdditionalDetailsEndpoint(
      order.id,
      mockValidRequestBody(additionalDetailsRequired = false, additionalDetails = null),
    ).expectStatus()
      .isOk

    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.offenceAdditionalDetails?.additionalDetails).isNull()
  }

  private fun callOffenceAdditionalDetailsEndpoint(
    orderId: UUID,
    body: String,
    username: String? = null,
  ): WebTestClient.ResponseSpec {
    val headers = if (username != null) setAuthorisation(username) else setAuthorisation()
    return webTestClient.put()
      .uri("/api/orders/$orderId/offence-additional-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          body,
        ),
      )
      .headers(headers)
      .exchange()
  }

  private fun mockValidRequestBody(
    id: UUID? = null,
    additionalDetailsRequired: Boolean? = true,
    additionalDetails: String? = "Default details text",
  ): String {
    val dto = UpdateOffenceAdditionalDetailsDto(
      id = id,
      additionalDetailsRequired = additionalDetailsRequired,
      additionalDetails = additionalDetails,
    )
    return objectMapper.writeValueAsString(dto)
  }
}
