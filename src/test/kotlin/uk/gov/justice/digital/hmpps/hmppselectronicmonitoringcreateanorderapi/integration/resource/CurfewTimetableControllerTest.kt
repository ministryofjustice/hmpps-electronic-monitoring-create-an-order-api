package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateCurfewTimetableDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ListItemValidationError
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.DayOfWeek
import java.util.UUID

class CurfewTimetableControllerTest : IntegrationTestBase() {
  @MockitoSpyBean
  lateinit var orderRepo: OrderRepository

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private object ErrorMessages {
    const val START_TIME_REQUIRED: String = "Enter time curfew starts"
    const val END_TIME_REQUIRED: String = "Enter time curfew ends"
    const val ADDRESS_REQUIRED: String = "Select where the device wearer will be during curfew hours"
  }

  @BeforeEach
  fun setup() {
    Mockito.reset(orderRepo)
    orderRepo.deleteAll()
  }

  @Test
  fun `Curfew timetable for an order created by a different user are not update-able`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-timetable")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockTimetableRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()
    val error = result.responseBody!!.first()
    Assertions.assertThat(
      error.developerMessage,
    ).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Curfew timetable for an order already submitted are not update-able`() {
    val order = createSubmittedOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-timetable")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockTimetableRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()
    val error = result.responseBody!!.first()
    Assertions.assertThat(
      error.developerMessage,
    ).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Should return errors when curfew timetable is invalid`() {
    val order = createSubmittedOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-timetable")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockInvalidTimetableRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ListItemValidationError::class.java)
      .returnResult()
    val error = result.responseBody!!
    Assertions.assertThat(error.count()).isEqualTo(3)
    Assertions.assertThat(error[0].index).isEqualTo(0)
    Assertions.assertThat(
      error[0].errors,
    ).contains(ValidationError("curfewAddress", ErrorMessages.ADDRESS_REQUIRED))
    Assertions.assertThat(error[1].index).isEqualTo(2)
    Assertions.assertThat(
      error[1].errors,
    ).contains(ValidationError("startTime", ErrorMessages.START_TIME_REQUIRED))
    Assertions.assertThat(error[2].index).isEqualTo(4)
    Assertions.assertThat(
      error[2].errors,
    ).contains(ValidationError("endTime", ErrorMessages.END_TIME_REQUIRED))
  }

  @Test
  fun `Should save order with updated timetable and cleared any previous timetable`() {
    val order = createOrder()

    // Create initial timetable
    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-timetable")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockTimetableRequestBody(
            order.id,
            startTime = "00:00:00",
            endTime = "07:00:00",
            curfewAddress = "SECONDARY_ADDRESS",
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    // Update timetable
    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-timetable")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockTimetableRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    // Get updated order
    val updatedOrder = getOrder(order.id)

    // Verify order state matches expected state
    Assertions.assertThat(updatedOrder.curfewTimeTable).hasSize(7)
    updatedOrder.curfewTimeTable.forEach {
      Assertions.assertThat(it.startTime).isEqualTo("19:00:00")
      Assertions.assertThat(it.endTime).isEqualTo("23:59:00")
      Assertions.assertThat(it.curfewAddress).isEqualTo("PRIMARY_ADDRESS")
    }
  }

  fun mockTimetableRequestBody(
    orderId: UUID,
    startTime: String? = "19:00:00",
    endTime: String? = "23:59:00",
    curfewAddress: String? = "PRIMARY_ADDRESS",
  ): String {
    val body = DayOfWeek.entries.map { day ->
      UpdateCurfewTimetableDto(
        dayOfWeek = day,
        startTime = startTime,
        endTime = endTime,
        curfewAddress = curfewAddress,
      )
    }.toList()
    return objectMapper.writeValueAsString(body)
  }

  fun mockInvalidTimetableRequestBody(orderId: UUID): String = """
      [
          {
              "dayOfWeek": "MONDAY",
              "orderId": "$orderId",            
              "startTime": "19:00:00",
              "endTime": "22:59:00"
          },         
          {
              "dayOfWeek": "TUESDAY",
             "orderId": "$orderId",
              "curfewAddress": "PRIMARY_ADDRESS",
              "startTime": "19:00:00",
              "endTime": "23:59:00"
          },
          {
              "dayOfWeek": "WEDNESDAY",
             "orderId": "$orderId",
              "curfewAddress": "PRIMARY_ADDRESS",           
              "endTime": "23:59:00"
          },
          {
              "dayOfWeek": "THURSDAY",
              "orderId": "$orderId",
              "curfewAddress": "PRIMARY_ADDRESS",
              "startTime": "19:00:00",
              "endTime": "23:59:00"
          },
          {
              "dayOfWeek": "FRIDAY",
             "orderId": "$orderId",
              "curfewAddress": "PRIMARY_ADDRESS",
             "startTime": "19:00:00"            
          },
          {
              "dayOfWeek": "SATURDAY",
             "orderId": "$orderId",
              "curfewAddress": "PRIMARY_ADDRESS",
              "startTime": "19:00:00",
              "endTime": "23:59:00"
          },
          {
              "dayOfWeek": "SUNDAY",
             "orderId": "$orderId",
              "curfewAddress": "PRIMARY_ADDRESS",
              "startTime": "19:00:00",
              "endTime": "23:59:00"
          },
          {
              "dayOfWeek": "SUNDAY",
              "orderId": "$orderId",
              "curfewAddress": "PRIMARY_ADDRESS,SECONDARY_ADDRESS,TERTIARY_ADDRESS",
              "startTime": "00:00:00",
              "endTime": "07:00:00"
          }
      ]
  """.trimIndent()
}
