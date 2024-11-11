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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

class InstallationAndRiskControllerTest : IntegrationTestBase() {
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
  fun `Installation and Risk for an order created by a different user are not update-able`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/installation-and-risk")
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
  fun `Installation and Risk for an order already submitted are not update-able`() {
    val order = createOrder()
    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/installation-and-risk")
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
  fun `Should save order with updated installation and risk`() {
    val order = createOrder()
    orderRepo.save(order)
    Mockito.reset(orderRepo)
    val mockRisk = mockValidRequestBody(
      order.id,
      "MockOffence",
      arrayOf("MockCategory"),
      "mockRisk",
      "mockMappaLevel",
      "mockMappaType",
    )
    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-and-risk")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRisk,
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
    argumentCaptor<Order>().apply {
      verify(orderRepo, Times(1)).save(capture())
      val updatedOrder = firstValue
      Assertions.assertThat(updatedOrder.installationAndRisk?.offence).isEqualTo("MockOffence")
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskCategory?.first()).isEqualTo("MockCategory")
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskDetails).isEqualTo("mockRisk")
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaLevel).isEqualTo("mockMappaLevel")
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaCaseType).isEqualTo("mockMappaType")
    }
  }

  @Test
  fun `Should save order with updated installation and risk will all default value`() {
    val order = createOrder()
    orderRepo.save(order)
    Mockito.reset(orderRepo)
    val mockRisk = mockValidRequestBody(
      order.id,
    )
    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-and-risk")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRisk,
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
    argumentCaptor<Order>().apply {
      verify(orderRepo, Times(1)).save(capture())
      val updatedOrder = firstValue
      Assertions.assertThat(updatedOrder.installationAndRisk?.offence).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskCategory).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.riskDetails).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaLevel).isNull()
      Assertions.assertThat(updatedOrder.installationAndRisk?.mappaCaseType).isNull()
    }
  }

  fun mockValidRequestBody(
    orderId: UUID,
    offence: String? = null,
    riskCategory: Array<String>? = null,
    riskDetails: String? = null,
    mappaLevel: String? = null,
    mappaCaseType: String? = null,
  ): String {
    val condition = InstallationAndRisk(
      orderId = orderId,
      offence = offence,
      riskCategory = riskCategory,
      riskDetails = riskDetails,
      mappaLevel = mappaLevel,
      mappaCaseType = mappaCaseType,
    )
    return objectMapper.writeValueAsString(condition)
  }
}
