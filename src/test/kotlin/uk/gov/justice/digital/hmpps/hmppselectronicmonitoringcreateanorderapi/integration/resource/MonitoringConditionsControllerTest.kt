package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MonitoringConditionsRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import java.util.*

class MonitoringConditionsControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var repo: MonitoringConditionsRepository

  @Autowired
  lateinit var orderFormRepo: OrderFormRepository

  private val mockOrderType: String = "mockOrderType"
  private val mockDevicesRequired: String = """["device1", "device2"]"""
  private val mockAcquisitiveCrime: Boolean = true
  private val mockDapol: Boolean = true
  private val mockCurfew: Boolean = true
  private val mockExclusionZone: Boolean = true
  private val mockTrail: Boolean = true
  private val mockMandatoryAttendance: Boolean = true
  private val mockAlcohol: Boolean = true

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Monitoring conditions can be updated with valid values`() {
    val order = createOrder()
    val updateMonitoringConditions = webTestClient.post()
      .uri("/api/order/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType",
              "devicesRequired": $mockDevicesRequired,
              "acquisitiveCrime": "$mockAcquisitiveCrime",
              "dapol": "$mockDapol",
              "curfew": "$mockCurfew",
              "exclusionZone": "$mockExclusionZone",
              "trail": "$mockTrail",
              "mandatoryAttendance": "$mockMandatoryAttendance",
              "alcohol": "$mockAlcohol"
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
    Assertions.assertThat(updateMonitoringConditions.responseBody?.orderType).isEqualTo(mockOrderType)
    Assertions.assertThat(updateMonitoringConditions.responseBody?.devicesRequired).isEqualTo(arrayOf("device1", "device2"))
    Assertions.assertThat(updateMonitoringConditions.responseBody?.acquisitiveCrime).isEqualTo(mockAcquisitiveCrime)
    Assertions.assertThat(updateMonitoringConditions.responseBody?.dapol).isEqualTo(mockDapol)
    Assertions.assertThat(updateMonitoringConditions.responseBody?.curfew).isEqualTo(mockCurfew)
    Assertions.assertThat(updateMonitoringConditions.responseBody?.exclusionZone).isEqualTo(mockExclusionZone)
    Assertions.assertThat(updateMonitoringConditions.responseBody?.trail).isEqualTo(mockTrail)
    Assertions.assertThat(updateMonitoringConditions.responseBody?.mandatoryAttendance)
      .isEqualTo(mockMandatoryAttendance)
    Assertions.assertThat(updateMonitoringConditions.responseBody?.alcohol).isEqualTo(mockAlcohol)
  }

  @Test
  fun `Monitoring conditions can be updated with null values`() {
    val order = createOrder()
    val updateMonitoringConditions = webTestClient.post()
      .uri("/api/order/${order.id}/contact-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "null",
              "devicesRequired": "null",
              "acquisitiveCrime": "null",
              "dapol": "null",
              "curfew": "null",
              "exclusionZone": "null",
              "trail": "null",
              "mandatoryAttendance": "null",
              "alcohol": "null"
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
    Assertions.assertThat(updateMonitoringConditions.responseBody?.orderType).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.devicesRequired).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.acquisitiveCrime).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.dapol).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.curfew).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.exclusionZone).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.trail).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.mandatoryAttendance).isNull()
    Assertions.assertThat(updateMonitoringConditions.responseBody?.alcohol).isNull()
  }

  @Test
  fun `Monitoring conditions cannot be updated by a different user`() {
    val order = createOrder()
    webTestClient.post()
      .uri("/api/order/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType"
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

    order.status = FormStatus.SUBMITTED
    orderFormRepo.save(order)

    webTestClient.post()
      .uri("/api/order/${order.id}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType"
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
    webTestClient.post()
      .uri("/api/order/${UUID.randomUUID()}/monitoring-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "orderType": "$mockOrderType"
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
