package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDapoDto
import java.time.ZoneId
import java.time.ZonedDateTime

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

    webTestClient.put()
      .uri("/api/orders/${order.id}/dapo")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(clause = "some clause", date = mockDate),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.dapoClauses.size).isEqualTo(1)
    val dapoClause = updatedOrder.dapoClauses.first()
    Assertions.assertThat(dapoClause.clause).isEqualTo("some clause")
    Assertions.assertThat(dapoClause.date).isEqualTo(mockDate)
  }

  private fun mockValidRequestBody(clause: String? = null, date: ZonedDateTime? = null): String {
    val dto = UpdateDapoDto(clause = clause, date = date)

    return objectMapper.writeValueAsString(dto)
  }
}
