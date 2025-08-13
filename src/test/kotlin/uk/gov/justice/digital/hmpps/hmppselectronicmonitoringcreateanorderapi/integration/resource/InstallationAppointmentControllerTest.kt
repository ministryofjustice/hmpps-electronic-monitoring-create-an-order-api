package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class InstallationAppointmentControllerTest : IntegrationTestBase() {

  private val appointmentDate = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(2).truncatedTo(ChronoUnit.SECONDS)

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Installation appointment cannot be updated by a different user`() {
    val order = createStoredOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-appointment")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "placeName": "Mock Place",
              "appointmentDate": "$appointmentDate"
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
  fun `Installation appointment cannot be updated for a submitted order`() {
    val order = createSubmittedOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-appointment")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "placeName": "Mock Place",
              "appointmentDate": "$appointmentDate"
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
  fun `Update appointment returns 400 if appointment date in the past`() {
    val order = createSubmittedOrder()
    val pastDate = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(-2)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/installation-appointment")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "placeName": "Mock Place",
              "appointmentDate": "$pastDate"
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
      ValidationError("appointmentDate", ValidationErrors.InstallationAppointment.APPOINTMENT_DATE_MUST_BE_IN_FUTURE),
    )
  }

  @Test
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
              "appointmentDate": "$appointmentDate"
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
      updatedOrder.installationAppointment.appointmentDate,
    ).isEqualTo(appointmentDate)
  }
}
