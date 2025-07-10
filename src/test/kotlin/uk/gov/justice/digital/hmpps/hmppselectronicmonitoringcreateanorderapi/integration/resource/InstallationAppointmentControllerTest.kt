package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import java.time.ZonedDateTime
import java.util.*

class InstallationAppointmentControllerTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Installation location cannot be updated by a different user`() {
    val order = createStoredOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-appointment")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "placeName": "Mock Place",
              "appointmentDate": "2024-01-01T00:00:00.000Z"
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
  fun `Installation location cannot be updated for a submitted order`() {
    val order = createSubmittedOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-appointment")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "placeName": "Mock Place",
              "appointmentDate": "2024-01-01T00:00:00.000Z"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  fun `It should store installation appointment to database`() {
    val order = createStoredOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-appointment")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "placeName": "Mock Place",
              "appointmentDate": "2024-01-01T00:00:00.000Z"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
    val updatedOrder = getOrder(order.id)
    assertThat(updatedOrder.installationAppointment!!.placeName).isEqualTo("Mock Place")
    assertThat(
      updatedOrder.installationAppointment!!.appointmentDate,
    ).isEqualTo(ZonedDateTime.parse("2024-01-01T00:00:00.000Z"))
  }
}
