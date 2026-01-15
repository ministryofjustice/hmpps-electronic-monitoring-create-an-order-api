package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDapoDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class DapoControllerTest : IntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `should add a dapo clause`() {
    val order = createOrder()
    val mockDate = ZonedDateTime.of(2025, 2, 2, 0, 0, 0, 0, ZoneId.of("UTC"))

    callDapoEndpoint(order.id, mockValidRequestBody(clause = "some clause", date = mockDate))
      .expectStatus()
      .isOk

    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.dapoClauses.size).isEqualTo(1)
    val dapoClause = updatedOrder.dapoClauses.first()
    Assertions.assertThat(dapoClause.clause).isEqualTo("some clause")
    Assertions.assertThat(dapoClause.date).isEqualTo(mockDate)
  }

  @Test
  fun `should update a dapo clause`() {
    val order = createOrder()
    val mockDate = ZonedDateTime.of(2025, 2, 2, 0, 0, 0, 0, ZoneId.of("UTC"))

    callDapoEndpoint(order.id, mockValidRequestBody(clause = "some clause", date = mockDate))
      .expectStatus()
      .isOk

    val orderWithDapo = getOrder(order.id)
    val dapoId = orderWithDapo.dapoClauses.first().id

    callDapoEndpoint(order.id, mockValidRequestBody(id = dapoId, clause = "some clause", date = mockDate))
      .expectStatus()
      .isOk

    val orderWithUpdatedDapo = getOrder(order.id)

    Assertions.assertThat(orderWithUpdatedDapo.dapoClauses.size).isEqualTo(1)
    val dapoClause = orderWithUpdatedDapo.dapoClauses.first()
    Assertions.assertThat(dapoClause.id).isEqualTo(dapoId)
    Assertions.assertThat(dapoClause.clause).isEqualTo("some clause")
    Assertions.assertThat(dapoClause.date).isEqualTo(mockDate)
  }

  @Test
  fun `it should return an error if the order was not created by the user`() {
    val order = createOrder()

    val result =
      callDapoEndpoint(
        order.id,
        mockValidRequestBody(clause = "some clause", date = ZonedDateTime.now()),
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
    callDapoEndpoint(UUID.randomUUID(), mockValidRequestBody(clause = "some clause", date = ZonedDateTime.now()))
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `it should return an error if the order is in a submitted state`() {
    val order = createSubmittedOrder()
    val result = callDapoEndpoint(
      order.id,
      mockValidRequestBody(clause = "some clause", date = ZonedDateTime.now()),
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
  fun `should return error if dapo is more than 20 chars`() {
    val order = createOrder()
    val result = callDapoEndpoint(
      order.id,
      mockValidRequestBody(clause = "some clause that is too long"),
    )
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("clause", ValidationErrors.Dapo.CAUSE_TOO_LONG),
    )
  }

  private fun callDapoEndpoint(orderId: UUID, body: String, username: String? = null): WebTestClient.ResponseSpec {
    val headers = if (username != null) setAuthorisation(username) else setAuthorisation()
    return webTestClient.put()
      .uri("/api/orders/$orderId/dapo")
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
    id: UUID? = UUID.randomUUID(),
    clause: String? = null,
    date: ZonedDateTime? = null,
  ): String {
    val dto = UpdateDapoDto(id = id, clause = clause, date = date)

    return objectMapper.writeValueAsString(dto)
  }
}
