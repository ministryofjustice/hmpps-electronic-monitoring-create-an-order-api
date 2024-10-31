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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.ZonedDateTime
import java.util.*

class CurfewReleaseDateControllerTest : IntegrationTestBase() {
  @SpyBean
  lateinit var orderRepo: OrderRepository

  @Autowired
  lateinit var objectMapper: ObjectMapper

  val mockReleaseDate: ZonedDateTime = ZonedDateTime.now().plusDays(1)

  @BeforeEach
  fun setup() {
    Mockito.reset(orderRepo)
    orderRepo.deleteAll()
  }

  @Test
  fun `Curfew release date for an order created by a different user are not update-able`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
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
    Assertions.assertThat(error.developerMessage).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Curfew release date for an order already submitted are not update-able`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
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
    Assertions.assertThat(error.developerMessage).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Should return errors when curfew release date is invalid`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(order.id, null, null, null, null),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    val error = result.responseBody!!
    Assertions.assertThat(result.responseBody).hasSize(4)
    Assertions.assertThat(error).contains(ValidationError("curfewAddress", "Curfew address is required"))
    Assertions.assertThat(error).contains(ValidationError("startTime", "Enter start time"))
    Assertions.assertThat(error).contains(ValidationError("endTime", "Enter end time"))
    Assertions.assertThat(error).contains(ValidationError("releaseDate", "Enter curfew release date"))
  }

  @Test
  fun `Should return errors when release date is in the past`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(order.id, ZonedDateTime.now().plusDays(-3), "19:00:00", "23:00:00", AddressType.PRIMARY),
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

    Assertions.assertThat(error).contains(ValidationError("releaseDate", "Curfew release date must be in the future"))
  }

  @Test
  fun `Should save order with updated release date `() {
    val order = createOrder()
    orderRepo.save(order)
    Mockito.reset(orderRepo)
    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
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
      Assertions.assertThat(updatedOrder.curfewReleaseDateConditions?.releaseDate).isEqualTo(mockReleaseDate)
      Assertions.assertThat(updatedOrder.curfewReleaseDateConditions?.startTime).isEqualTo("19:00:00")
      Assertions.assertThat(updatedOrder.curfewReleaseDateConditions?.endTime).isEqualTo("23:59:00")
      Assertions.assertThat(updatedOrder.curfewReleaseDateConditions?.curfewAddress).isEqualTo(AddressType.PRIMARY)
    }
  }

  fun mockValidRequestBody(
    orderId: UUID,
    releaseDate: ZonedDateTime? = mockReleaseDate,
    startTime: String? = "19:00:00",
    endTime: String? = "23:59:00",
    curfewAddress: AddressType? = AddressType.PRIMARY,
  ): String {
    return mockRequestBody(orderId, releaseDate, startTime, endTime, curfewAddress)
  }

  fun mockRequestBody(
    orderId: UUID,
    releaseDate: ZonedDateTime?,
    startTime: String?,
    endTime: String?,
    curfewAddress: AddressType?,
  ): String {
    val condition = CurfewReleaseDateConditions(
      orderId = orderId,
      releaseDate = releaseDate,
      startTime = startTime,
      endTime = endTime,
      curfewAddress = curfewAddress,
    )
    return objectMapper.writeValueAsString(condition)
  }
}
