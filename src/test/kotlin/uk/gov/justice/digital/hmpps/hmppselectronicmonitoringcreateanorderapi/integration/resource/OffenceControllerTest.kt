package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceDto
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class OffenceControllerTest : IntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `should add an offence`() {
    val order = createOrder()
    val mockDate = ZonedDateTime.of(2025, 2, 2, 0, 0, 0, 0, ZoneId.of("UTC"))

    callOffenceEndpoint(order.id, mockValidRequestBody(offenceType = "some offence", offenceDate = mockDate))
      .expectStatus()
      .isOk

    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.offences.size).isEqualTo(1)
    val offence = updatedOrder.offences.first()
    Assertions.assertThat(offence.offenceType).isEqualTo("some offence")
    Assertions.assertThat(offence.offenceDate).isEqualTo(mockDate)
  }

  @Test
  fun `should update an offence`() {
    val order = createOrder()
    val mockDate = ZonedDateTime.of(2025, 2, 2, 0, 0, 0, 0, ZoneId.of("UTC"))

    callOffenceEndpoint(order.id, mockValidRequestBody(offenceType = "some offence", offenceDate = mockDate))
      .expectStatus()
      .isOk

    val orderWithOffence = getOrder(order.id)
    val dapoId = orderWithOffence.offences.first().id

    callOffenceEndpoint(
      order.id,
      mockValidRequestBody(id = dapoId, offenceType = "some offence", offenceDate = mockDate),
    )
      .expectStatus()
      .isOk

    val orderWithUpdatedOffence = getOrder(order.id)

    Assertions.assertThat(orderWithUpdatedOffence.offences.size).isEqualTo(1)
    val offence = orderWithUpdatedOffence.offences.first()
    Assertions.assertThat(offence.id).isEqualTo(dapoId)
    Assertions.assertThat(offence.offenceType).isEqualTo("some offence")
    Assertions.assertThat(offence.offenceDate).isEqualTo(mockDate)
  }

  @Test
  fun `it should return an error if the order was not created by the user`() {
    val order = createOrder()

    val result =
      callOffenceEndpoint(
        order.id,
        mockValidRequestBody(offenceType = "some offence", offenceDate = ZonedDateTime.now()),
        username = "AUTH_ADM_2",
      )
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
    callOffenceEndpoint(UUID.randomUUID(), mockValidRequestBody())
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `it should return an error if the order is in a submitted state`() {
    val order = createSubmittedOrder()

    val result = callOffenceEndpoint(
      order.id,
      mockValidRequestBody(),
    )
      .expectStatus()
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()

    val error = result.responseBody!!.first()
    Assertions.assertThat(
      error.developerMessage,
    ).isEqualTo("An editable order with ${order.id} does not exist")
  }

  private fun callOffenceEndpoint(orderId: UUID, body: String, username: String? = null): WebTestClient.ResponseSpec {
    val headers = if (username != null) setAuthorisation(username) else setAuthorisation()
    return webTestClient.put()
      .uri("/api/orders/$orderId/offence")
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
    id: UUID = UUID.randomUUID(),
    offenceType: String? = null,
    offenceDate: ZonedDateTime? = null,
  ): String {
    val dto = UpdateOffenceDto(id = id, offenceType = offenceType, offenceDate = offenceDate)

    return objectMapper.writeValueAsString(dto)
  }
}
