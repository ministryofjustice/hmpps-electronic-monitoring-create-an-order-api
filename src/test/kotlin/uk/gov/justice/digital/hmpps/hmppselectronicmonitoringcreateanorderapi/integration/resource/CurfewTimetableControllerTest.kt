package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.internal.verification.Times
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ListItemValidationError
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.DayOfWeek
import java.util.UUID

class CurfewTimetableControllerTest : IntegrationTestBase() {
  @SpyBean
  lateinit var orderRepo: OrderRepository

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun setup() {
    Mockito.reset(orderRepo)
    orderRepo.deleteAll()
  }

  @Test
  fun `Curfew timetable for an order order created by a different user are not update-able`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-timetable")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()
    val error = result.responseBody!!.first()
    Assertions.assertThat(error.developerMessage).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Curfew timetable zone for an order already submitted are not update-able`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-timetable")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()
    val error = result.responseBody!!.first()
    Assertions.assertThat(error.developerMessage).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Should return errors when curfew timetable is invalid`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-timetable")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockInvalidREquestBody(order.id),
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
    Assertions.assertThat(error[0].errors).contains(ValidationError("curfewAddress", "Curfew address is required"))
    Assertions.assertThat(error[1].index).isEqualTo(2)
    Assertions.assertThat(error[1].errors).contains(ValidationError("startTime", "Enter start time of curfew"))
    Assertions.assertThat(error[2].index).isEqualTo(4)
    Assertions.assertThat(error[2].errors).contains(ValidationError("endTime", "Enter end time of curfew"))
  }

  @Test
  fun `Should save order with updated timetable and cleared any previous timetable`() {
    val order = createOrder()
    val timetable = DayOfWeek.entries.map { day -> CurfewTimeTable(dayOfWeek = day, orderId = order.id, startTime = "00:00:00", endTime = "07:00:00", curfewAddress = "SECONDARY_ADDRESS") }.toList()
    order.curfewTimeTable.addAll(timetable)
    orderRepo.save(order)
    Mockito.reset(orderRepo)
    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-timetable")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
    argumentCaptor<Order>().apply {
      verify(orderRepo, Times(1)).save(capture())
      val updatedOrder = firstValue
      Assertions.assertThat(updatedOrder.curfewTimeTable.count()).isEqualTo(7)
      updatedOrder.curfewTimeTable.forEach {
        Assertions.assertThat(it.startTime).isEqualTo("19:00:00")
        Assertions.assertThat(it.endTime).isEqualTo("23:59:00")
        Assertions.assertThat(it.curfewAddress).isEqualTo("PRIMARY_ADDRESS")
      }
    }
  }

  fun mockRequestBody(
    orderId: UUID,
    startTime: String? = "19:00:00",
    endTime: String? = "23:59:00",
    curfewAddress: String? = "PRIMARY_ADDRESS",
  ): String {
    val body = DayOfWeek.entries.map { day -> CurfewTimeTable(dayOfWeek = day, orderId = orderId, startTime = startTime, endTime = endTime, curfewAddress = curfewAddress) }.toList()
    return objectMapper.writeValueAsString(body)
  }

  fun mockInvalidREquestBody(orderId: UUID): String {
    return """
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
}
