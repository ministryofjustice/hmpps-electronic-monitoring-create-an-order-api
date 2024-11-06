package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MonitoringConditionsAlcoholRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class MonitoringConditionsAlcoholControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var alcoholMonitoringConditionsRepo: MonitoringConditionsAlcoholRepository

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

  private val mockValidAlcoholMonitoringConditions: String = """
    {
      "monitoringType": "ALCOHOL_ABSTINENCE",
      "startDate": "$mockStartDate",
      "endDate": "$mockEndDate",
      "installationLocation": "PRIMARY",
      "prisonName": null,
      "probationOfficeName": null
    }
  """.trimIndent()

  @BeforeEach
  fun setup() {
    alcoholMonitoringConditionsRepo.deleteAll()
    orderRepo.deleteAll()
  }

  @Test
  fun `Alcohol monitoring conditions cannot be updated by a different user`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-alcohol")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "monitoringType": "ALCOHOL_ABSTINENCE",
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate",
              "installationLocation": "PRIMARY",
              "prisonName": null,
              "probationOfficeName": null
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
  fun `Alcohol monitoring conditions for a non-existent order are not update-able`() {
    webTestClient.put()
      .uri("/api/orders/${UUID.randomUUID()}/monitoring-conditions-alcohol")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(mockValidAlcoholMonitoringConditions),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `Alcohol monitoring conditions cannot be updated for a submitted order`() {
    val order = createOrder()

    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-alcohol")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(mockValidAlcoholMonitoringConditions),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `Alcohol monitoring conditions can be updated`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-alcohol")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(mockValidAlcoholMonitoringConditions),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(AlcoholMonitoringConditions::class.java)
      .returnResult()

    val alcoholConditions = result.responseBody!!

    Assertions.assertThat(alcoholConditions.startDate).isEqualTo(mockStartDate)
    Assertions.assertThat(alcoholConditions.endDate).isEqualTo(mockEndDate)
    Assertions.assertThat(
      alcoholConditions.monitoringType,
    ).isEqualTo(AlcoholMonitoringType.ALCOHOL_ABSTINENCE)
    Assertions.assertThat(
      alcoholConditions.installationLocation,
    ).isEqualTo(AlcoholMonitoringInstallationLocationType.PRIMARY)
    Assertions.assertThat(alcoholConditions.prisonName).isEqualTo(null)
    Assertions.assertThat(alcoholConditions.probationOfficeName).isEqualTo(null)
  }

  @Test
  fun `StartDate is mandatory for alcohol monitoring conditions`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-alcohol")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "monitoringType": "ALCOHOL_ABSTINENCE",
              "startDate": "",
              "endDate": "$mockEndDate",
              "installationLocation": "PRIMARY",
              "prisonName": null,
              "probationOfficeName": null
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
  fun `Monitoring Type is mandatory for alcohol monitoring conditions`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-alcohol")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "monitoringType": null,
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate",
              "installationLocation": "PRIMARY",
              "prisonName": null,
              "probationOfficeName": null
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
      ValidationError("monitoringType", "Monitoring type is required"),
    )
  }

  @Test
  fun `Installation location is mandatory for alcohol monitoring conditions`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-alcohol")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "monitoringType": "ALCOHOL_ABSTINENCE",
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate",
              "installationLocation": null,
              "prisonName": null,
              "probationOfficeName": null
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
      ValidationError("installationLocation", "Installation location is required"),
    )
  }

  @Test
  fun `Alcohol monitoring conditions cannot be updated if startDate is in the past`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-alcohol")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "monitoringType": "ALCOHOL_ABSTINENCE",
              "startDate": "${ZonedDateTime.parse("1990-01-01T00:00:00.000Z")}",
              "endDate": "$mockEndDate",
              "installationLocation": "PRIMARY",
              "prisonName": null,
              "probationOfficeName": null
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
  fun `Alcohol monitoring conditions cannot be updated if endDate is in the past`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-alcohol")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "monitoringType": "ALCOHOL_ABSTINENCE",
              "startDate": "$mockStartDate",
              "endDate": "${ZonedDateTime.parse("1990-01-01T00:00:00.000Z")}",
              "installationLocation": "PRIMARY",
              "prisonName": null,
              "probationOfficeName": null
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
