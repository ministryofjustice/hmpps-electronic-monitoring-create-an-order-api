package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MonitoringConditionsTrailRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class MonitoringConditionsTrailControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var trailMonitoringConditionsRepo: MonitoringConditionsTrailRepository

  @Autowired
  lateinit var orderRepo: OrderRepository

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

  @BeforeEach
  fun setup() {
    trailMonitoringConditionsRepo.deleteAll()
    orderRepo.deleteAll()
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
    val order = createOrder()

    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)

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
      ValidationError("startDate", "Start date is required"),
    )
  }

  @Test
  fun `Trail monitoring conditions cannot be updated if startDate is in the past`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-trail")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "startDate": "${ZonedDateTime.parse("1990-01-01T00:00:00.000Z")}",
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
      ValidationError("startDate", "Start date must be in the future"),
    )
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
      ValidationError("endDate", "End date must be in the future"),
    )
  }
}
