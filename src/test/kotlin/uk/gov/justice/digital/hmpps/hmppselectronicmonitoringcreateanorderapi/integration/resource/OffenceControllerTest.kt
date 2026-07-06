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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UpdateOrderIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UriTestCase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceDto
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class OffenceControllerTest : UpdateOrderIntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper
  val offenceId: UUID = UUID.randomUUID()
  override val testUris: List<UriTestCase> = listOf(
    UriTestCase(uri = "/api/orders/:orderId/offence", createValidBody = {
      mockValidRequestBody(offenceType = "some offence", offenceDate = ZonedDateTime.now())
    }, httpMethod = HttpMethod.PUT),
    UriTestCase(uri = "/api/orders/:orderId/offence/delete/$offenceId", createValidBody = {
      ""
    }, httpMethod = HttpMethod.DELETE),
  )

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
      mockValidRequestBody(id = dapoId, offenceType = "other offence", offenceDate = mockDate),
    )
      .expectStatus()
      .isOk

    val orderWithUpdatedOffence = getOrder(order.id)

    Assertions.assertThat(orderWithUpdatedOffence.offences.size).isEqualTo(1)
    val offence = orderWithUpdatedOffence.offences.first()
    Assertions.assertThat(offence.id).isEqualTo(dapoId)
    Assertions.assertThat(offence.offenceType).isEqualTo("other offence")
    Assertions.assertThat(offence.offenceDate).isEqualTo(mockDate)
  }

  @Test
  fun `can remove an offence`() {
    val order = createStoredOrder()
    order.offences.add(
      Offence(
        versionId = order.versions.first().id,
        id = offenceId,
        offenceType = "THEFT_OFFENCES",
        offenceDate = ZonedDateTime.now(),
      ),
    )
    repo.save(order)

    webTestClient.delete()
      .uri("/api/orders/${order.id}/offence/delete/$offenceId")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNoContent

    val updatedOrder = getOrder(order.id)
    Assertions.assertThat(updatedOrder.offences).isEmpty()
  }

  @Test
  fun `should set multiple offences in a single request`() {
    val order = createOrder()

    callOffenceEndpoint(order.id, mockMultiOffenceRequestBody(listOf("THEFT_OFFENCES", "SEXUAL_OFFENCES")))
      .expectStatus()
      .isOk

    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.offences.size).isEqualTo(2)
    Assertions.assertThat(updatedOrder.offences.map { it.offenceType })
      .containsExactlyInAnyOrder("THEFT_OFFENCES", "SEXUAL_OFFENCES")
    Assertions.assertThat(updatedOrder.offences).allSatisfy {
      Assertions.assertThat(it.offenceDate).isNull()
    }
  }

  @Test
  fun `setting offences returns the full list in the response`() {
    val order = createOrder()

    val result = callOffenceEndpoint(
      order.id,
      mockMultiOffenceRequestBody(listOf("THEFT_OFFENCES", "SEXUAL_OFFENCES")),
    )
      .expectStatus()
      .isOk
      .expectBodyList(Offence::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody!!.map { it.offenceType })
      .containsExactlyInAnyOrder("THEFT_OFFENCES", "SEXUAL_OFFENCES")
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

  private fun mockMultiOffenceRequestBody(offences: List<String>): String {
    val dto = UpdateOffenceDto(offences = offences)
    return objectMapper.writeValueAsString(dto)
  }
}
