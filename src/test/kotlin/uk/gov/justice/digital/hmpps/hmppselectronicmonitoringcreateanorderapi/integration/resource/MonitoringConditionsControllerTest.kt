package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class MonitoringConditionsControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var orderRepo: OrderRepository

  private val mockOrderType = OrderType.COMMUNITY
  private val mockOrderTypeDescription = OrderTypeDescription.DAPOL
  private val mockConditionType = MonitoringConditionType.LICENSE_CONDITION_OF_A_CUSTODIAL_ORDER
  private val mockStartDate = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(1)
  private val mockEndDate = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(2)

  @BeforeEach
  fun setup() {
    orderRepo.deleteAll()
  }

  @Test
  fun `Monitoring conditions can be updated with valid values`() {
    val order = createOrder()
    val updateMonitoringConditions = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "acquisitiveCrime": "true",
              "dapol": "true",
              "curfew": "true",
              "exclusionZone": "true",
              "trail": "true",
              "mandatoryAttendance": "true",
              "alcohol": "true",
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
      .expectBody(MonitoringConditions::class.java)
      .returnResult()

    Assertions.assertThat(updateMonitoringConditions.responseBody?.orderId).isEqualTo(order.id)
    Assertions.assertThat(
      updateMonitoringConditions.responseBody?.orderType,
    ).isEqualTo(mockOrderType)
    Assertions.assertThat(
      updateMonitoringConditions.responseBody?.orderTypeDescription,
    ).isEqualTo(mockOrderTypeDescription)
    Assertions.assertThat(
      updateMonitoringConditions.responseBody?.conditionType,
    ).isEqualTo(mockConditionType)
    Assertions.assertThat(updateMonitoringConditions.responseBody?.curfew).isTrue()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.exclusionZone).isTrue()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.trail).isTrue()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.mandatoryAttendance)
      .isTrue()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.alcohol).isTrue()
  }

  @Test
  fun `isValid is false when mandatory fields are not populated`() {
    val order = createOrder()
    Assertions.assertThat(order.monitoringConditions?.isValid).isFalse()
  }

  @Test
  fun `isValid is true when mandatory fields are populated`() {
    val order = createOrder()
    val updateMonitoringConditions = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "curfew": true,
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "startDate": "$mockStartDate"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(MonitoringConditions::class.java)
      .returnResult()

    Assertions.assertThat(updateMonitoringConditions.responseBody?.isValid).isTrue()
  }

  @Test
  fun `Non-mandatory monitoring conditions can be updated with null values`() {
    val order = createOrder()
    val updateMonitoringConditions = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "acquisitiveCrime": null,
              "dapol": null,
              "curfew": "true",
              "exclusionZone": "true",
              "trail": "true",
              "mandatoryAttendance": "true",
              "alcohol": "true",
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
      .expectBody(MonitoringConditions::class.java)
      .returnResult()

    Assertions.assertThat(updateMonitoringConditions.responseBody?.orderId).isEqualTo(order.id)
  }

  @Test
  fun `Update monitoring conditions returns 400 if invalid data`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": null,
              "orderTypeDescription": null,
              "conditionType": null,
              "acquisitiveCrime": null,
              "dapol": null,
              "curfew": "true",
              "exclusionZone": "true",
              "trail": "true",
              "mandatoryAttendance": "true",
              "alcohol": "true",
              "startDate": null,
              "endDate": null
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
    Assertions.assertThat(result.responseBody).hasSize(3)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("orderType", "Order type is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("conditionType", "Condition type is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("startDate", "Monitoring conditions start date is required"),
    )
  }

  @Test
  fun `Update monitoring conditions allows start date in the past`() {
    val mockPastStartDate = ZonedDateTime.now(ZoneId.of("UTC")).minusMonths(1)
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "acquisitiveCrime": "true",
              "dapol": "true",
              "curfew": "true",
              "exclusionZone": "true",
              "trail": "true",
              "mandatoryAttendance": "true",
              "alcohol": "true",
              "startDate": "$mockPastStartDate",
              "endDate": null
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(MonitoringConditions::class.java)
  }

  @Test
  fun `Update monitoring conditions returns 400 if end date is before start date`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "acquisitiveCrime": "true",
              "dapol": "true",
              "curfew": "true",
              "exclusionZone": "true",
              "trail": "true",
              "mandatoryAttendance": "true",
              "alcohol": "true",
              "startDate": "$mockStartDate",
              "endDate": "${mockStartDate.plusDays(-10)}"
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
      ValidationError("endDate", "End date must be after start date"),
    )
  }

  @Test
  fun `Form cannot be submitted if no Monitoring Types are selected`() {
    val order = createOrder()
    val updateMonitoringConditions = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "acquisitiveCrime": null,
              "dapol": null,
              "curfew": null,
              "exclusionZone": null,
              "trail": null,
              "mandatoryAttendance": null,
              "alcohol": null,
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `Form can be submitted if one Monitoring Type is selected`() {
    val order = createOrder()
    val updateMonitoringConditions = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "acquisitiveCrime": null,
              "dapol": null,
              "curfew": "true",
              "exclusionZone": null,
              "trail": null,
              "mandatoryAttendance": null,
              "alcohol": null,
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
      .expectBody(MonitoringConditions::class.java)
      .returnResult()

    Assertions.assertThat(updateMonitoringConditions.responseBody?.orderId).isEqualTo(order.id)
    Assertions.assertThat(updateMonitoringConditions.responseBody?.curfew).isTrue()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.exclusionZone).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.trail).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.mandatoryAttendance).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.alcohol).isNull()
  }

  @Test
  fun `Monitoring conditions cannot be updated by a different user`() {
    val order = createOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "acquisitiveCrime": "true",
              "dapol": "true",
              "curfew": "true",
              "exclusionZone": "true",
              "trail": "true",
              "mandatoryAttendance": "true",
              "alcohol": "true",
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
  fun `Monitoring conditions cannot be updated for a submitted order`() {
    val order = createOrder()

    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "acquisitiveCrime": "true",
              "dapol": "true",
              "curfew": "true",
              "exclusionZone": "true",
              "trail": "true",
              "mandatoryAttendance": "true",
              "alcohol": "true",
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
  fun `Monitoring conditions for a non-existent order are not accessible`() {
    createOrder()
    webTestClient.put()
      .uri("/api/orders/${UUID.randomUUID()}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "orderTypeDescription": "$mockOrderTypeDescription",
              "conditionType": "$mockConditionType",
              "acquisitiveCrime": "true",
              "dapol": "true",
              "curfew": "true",
              "exclusionZone": "true",
              "trail": "true",
              "mandatoryAttendance": "true",
              "alcohol": "true",
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
}
