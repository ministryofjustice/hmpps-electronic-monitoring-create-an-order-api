package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class MonitoringConditionsTrailControllerTest : IntegrationTestBase() {

  private val mockStartDate: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(2025, 1, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )
  private val mockEndDate: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(2030, 1, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )

  private object ErrorMessages {
    const val START_DATE_REQUIRED: String = "Enter date trail monitoring starts"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "End date must be in the future"
  }

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Trail monitoring conditions cannot be updated by a different user`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-trail")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate"
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
  fun `Trail monitoring conditions for a non-existent order are not update-able`() {
    webTestClient.put()
      .uri("/api/orders/${UUID.randomUUID()}/monitoring-conditions-trail")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate"
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
  fun `Trail monitoring conditions cannot be updated for a submitted order`() {
    val order = createSubmittedOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-trail")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate"
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
  fun `Trail monitoring conditions can be updated`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-trail")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(TrailMonitoringConditions::class.java)
      .returnResult()

    val trailConditions = result.responseBody!!

    Assertions.assertThat(trailConditions.startDate).isEqualTo(mockStartDate)
    Assertions.assertThat(trailConditions.endDate).isEqualTo(mockEndDate)
  }

  @Test
  fun `StartDate is mandatory for trail monitoring conditions`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-trail")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "startDate": "",
              "endDate": "$mockEndDate"
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
      ValidationError("startDate", ErrorMessages.START_DATE_REQUIRED),
    )
  }

  @Test
  fun `Trail monitoring conditions can be updated with a startDate in the past`() {
    val mockPastStartDate: ZonedDateTime = ZonedDateTime.of(
      LocalDate.of(1990, 1, 1),
      LocalTime.NOON,
      ZoneId.of("UTC"),
    )
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-trail")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "startDate": "$mockPastStartDate",
              "endDate": "$mockEndDate"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(TrailMonitoringConditions::class.java)
      .returnResult()
    val trailConditions = result.responseBody!!

    Assertions.assertThat(trailConditions.startDate).isEqualTo(mockPastStartDate)
    Assertions.assertThat(trailConditions.endDate).isEqualTo(mockEndDate)
  }

  @Test
  fun `Trail monitoring conditions cannot be updated if endDate is in the past`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-trail")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "startDate": "$mockStartDate",
              "endDate": "${ZonedDateTime.parse("1990-01-01T00:00:00.000Z")}"
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
      ValidationError("endDate", ErrorMessages.END_DATE_MUST_BE_IN_FUTURE),
    )
  }
}
