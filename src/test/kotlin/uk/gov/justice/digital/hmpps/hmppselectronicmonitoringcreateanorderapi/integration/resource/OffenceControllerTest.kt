package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceDto
import java.time.ZoneId
import java.time.ZonedDateTime

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

    webTestClient.put()
      .uri("/api/orders/${order.id}/offence")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(offenceType = "some offence", offenceDate = mockDate),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.offences.size).isEqualTo(1)
    val offence = updatedOrder.offences.first()
    Assertions.assertThat(offence.offenceType).isEqualTo("some offence")
    Assertions.assertThat(offence.offenceDate).isEqualTo(mockDate)
  }

  private fun mockValidRequestBody(offenceType: String? = null, offenceDate: ZonedDateTime? = null): String {
    val dto = UpdateOffenceDto(offenceType = offenceType, offenceDate = offenceDate)

    return objectMapper.writeValueAsString(dto)
  }
}
