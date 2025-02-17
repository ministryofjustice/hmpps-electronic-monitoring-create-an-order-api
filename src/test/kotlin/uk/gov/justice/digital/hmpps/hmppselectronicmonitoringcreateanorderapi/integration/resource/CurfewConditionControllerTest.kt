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
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class CurfewConditionControllerTest : IntegrationTestBase() {
  @MockitoSpyBean
  lateinit var orderRepo: OrderRepository

  @Autowired
  lateinit var objectMapper: ObjectMapper

  val mockStartDate: ZonedDateTime = ZonedDateTime.now().plusDays(1)
  val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusDays(3)
  private val mockPastStartDate = ZonedDateTime.of(
    LocalDate.of(1970, 2, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )
  private val mockPastEndDate = mockPastStartDate.plusDays(1)

  @BeforeEach
  fun setup() {
    Mockito.reset(orderRepo)
    orderRepo.deleteAll()
  }

  @Test
  fun `Curfew release date for an order created by a different user are not update-able`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(order.id),
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
  fun `Curfew release date for an order already submitted are not update-able`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(order.id),
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
  fun `Should return errors when curfew condition is invalid`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(order.id, null, null, null),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    val error = result.responseBody!!
    Assertions.assertThat(result.responseBody).hasSize(2)
    Assertions.assertThat(
      error,
    ).contains(ValidationError("curfewAddress", "Curfew address is required"))
    Assertions.assertThat(error).contains(ValidationError("startDate", "Enter curfew start day"))
  }

  @Test
  fun `Should return errors when end date is in the past`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            order.id,
            startDate = mockPastStartDate,
            endDate = mockPastEndDate,
            "PRIMARY",
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    val error = result.responseBody!!

    Assertions.assertThat(result.responseBody).hasSize(1)
    Assertions.assertThat(
      error,
    ).contains(ValidationError("endDate", "Curfew end day must be in the future"))
  }

  @Test
  fun `Should not return error when curfew conditions start date is in the past`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            order.id,
            startDate = mockPastStartDate,
            endDate = mockEndDate,
            "PRIMARY",
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(CurfewConditions::class.java)
  }

  @Test
  fun `Should return errors when end date is before start date`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            order.id,
            ZonedDateTime.now().plusDays(3),
            ZonedDateTime.now().plusDays(2),
            "PRIMARY",
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    val error = result.responseBody!!
    Assertions.assertThat(result.responseBody).hasSize(1)

    Assertions.assertThat(
      error,
    ).contains(ValidationError("endDate", "End date must be after start date"))
  }

  @Test
  fun `Should save order with updated release date `() {
    val order = createOrder()
    orderRepo.save(order)
    Mockito.reset(orderRepo)
    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
    argumentCaptor<Order>().apply {
      verify(orderRepo, Times(1)).save(capture())
      val updatedOrder = firstValue
      Assertions.assertThat(updatedOrder.curfewConditions?.startDate).isEqualTo(mockStartDate)
      Assertions.assertThat(updatedOrder.curfewConditions?.endDate).isEqualTo(mockEndDate)
      Assertions.assertThat(
        updatedOrder.curfewConditions?.curfewAddress,
      ).isEqualTo("PRIMARY,SECONDARY")
    }
  }

  fun mockValidRequestBody(
    orderId: UUID,
    startDate: ZonedDateTime? = mockStartDate,
    endDate: ZonedDateTime? = mockEndDate,
    curfewAddress: String? = "PRIMARY,SECONDARY",
  ): String {
    return mockRequestBody(orderId, startDate, endDate, curfewAddress)
  }

  fun mockRequestBody(
    orderId: UUID,
    startDate: ZonedDateTime?,
    endDate: ZonedDateTime?,
    curfewAddress: String?,
  ): String {
    val condition = CurfewConditions(
      orderId = orderId,
      startDate = startDate,
      endDate = endDate,
      curfewAddress = curfewAddress,
    )
    return objectMapper.writeValueAsString(condition)
  }
}
