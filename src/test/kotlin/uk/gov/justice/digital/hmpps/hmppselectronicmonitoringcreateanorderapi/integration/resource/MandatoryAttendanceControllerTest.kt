package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UpdateOrderIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UriTestCase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import java.time.ZonedDateTime
import java.util.*

class MandatoryAttendanceControllerTest : UpdateOrderIntegrationTestBase() {
  private val mockId: UUID = UUID.randomUUID()
  private val mockStartDate: ZonedDateTime = ZonedDateTime.now()
  private val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusDays(1)
  private val mockPurpose: String = "Purpose"
  private val mockAppointmentDay: String = "Day"
  private val mockStartTime: String = "12:00"
  private val mockEndTime: String = "13:00"
  private val mockAddressLine1: String = "mockAddressLine1"
  private val mockAddressLine2: String = "mockAddressLine2"
  private val mockAddressLine3: String = "mockAddressLine3"
  private val mockAddressLine4: String = "mockAddressLine4"
  private val mockPostcode: String = "mockPostcode"

  override val testUris: List<UriTestCase> = listOf(
    UriTestCase(uri = "/api/orders/:orderId/mandatory-attendance", createValidBody = {
      """
            {
              "id": "$mockId",
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate",
              "purpose": "$mockPurpose",
              "appointmentDay": "$mockAppointmentDay",
              "startTime": "$mockStartTime",
              "endTime": "$mockEndTime",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": "$mockPostcode"
            }
      """.trimIndent()
    }),
  )

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Mandatory Attendance details can be created with valid data`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/mandatory-attendance")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "id": "$mockId",
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate",
              "purpose": "$mockPurpose",
              "appointmentDay": "$mockAppointmentDay",
              "startTime": "$mockStartTime",
              "endTime": "$mockEndTime",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": "$mockPostcode"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `Mandatory Attendance details can be updated multiple times`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/mandatory-attendance")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "id": "$mockId",
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate",
              "purpose": "$mockPurpose",
              "appointmentDay": "$mockAppointmentDay",
              "startTime": "$mockStartTime",
              "endTime": "$mockEndTime",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": "$mockPostcode"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk

    webTestClient.put()
      .uri("/api/orders/${order.id}/mandatory-attendance")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "id": "$mockId",
              "startDate": "$mockStartDate",
              "endDate": "$mockEndDate",
              "purpose": "$mockPurpose",
              "appointmentDay": "$mockAppointmentDay",
              "startTime": "$mockStartTime",
              "endTime": "$mockEndTime",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": "$mockPostcode"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `Mandatory Attendance details cannot be created with invalid data`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/mandatory-attendance")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "id": "$mockId",
              "startDate": "",
              "endDate": "",
              "purpose": "",
              "appointmentDay": "",
              "startTime": "",
              "endTime": "",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": ""
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
  fun `Mandatory Attendance monitoring details can store monitoring start date`() {
    val order = storedOrderWithNoMonitoringConditionDateWindow()
    val earlierDate = ZonedDateTime.parse("2000-01-01T00:00:00Z")

    order.monitoringConditions = MonitoringConditions(
      versionId = order.versions.first().id,
      mandatoryAttendance = true,
    )
    repo.save(order)

    webTestClient.put()
      .uri("/api/orders/${order.id}/mandatory-attendance")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "id": "${order.id}",
              "startDate": "$earlierDate",
              "endDate": "$mockEndDate",
              "purpose": "$mockPurpose",
              "appointmentDay": "$mockAppointmentDay",
              "startTime": "$mockStartTime",
              "endTime": "$mockEndTime",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": "$mockPostcode"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk

    val updatedOrder = getOrder(order.id)
    assertThat(
      updatedOrder.mandatoryAttendanceConditions.first().startDate!!.toInstant(),
    ).isEqualTo(earlierDate.toInstant())
    assertThat(updatedOrder.monitoringConditions!!.startDate!!.toInstant()).isEqualTo(earlierDate.toInstant())
  }
}
