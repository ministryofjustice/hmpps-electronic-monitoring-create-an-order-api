package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UpdateOrderIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderParameters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateIsMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class MappaControllerTest : IntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `should update mappa`() {
    val order = createOrder()

    val response = webTestClient.put()
      .uri("/api/orders/${order.id}/mappa")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "level": "MAPPA_ONE",
              "category": "CATEGORY_ONE"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .returnResult()

    assertThat(response.responseBody).isNotNull()
  }

  @Test
  fun `mappa for an order created by a different user are not update-able`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/mappa")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "level": "MAPPA_ONE",
              "category": "CATEGORY_ONE"
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
  fun `mappa for a non-existent order are not update-able`() {
    webTestClient.put()
      .uri("/api/orders/${UUID.randomUUID()}/mappa")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "level": "MAPPA_ONE",
              "category": "CATEGORY_ONE"
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
  fun `mappa for a submitted order are not update-able`() {
    val order = createSubmittedOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/mappa")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "level": "MAPPA_ONE",
              "category": "CATEGORY_ONE"
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

class UpdateIsMappaTest : UpdateOrderIntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  override val uri = "/api/orders/:orderId/mappa/is-mappa"

  override fun createValidBody(): String = mockRequestBody(isMappa = true)

  @Test
  fun `should update isMappa`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/mappa/is-mappa")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(true),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(OrderParameters::class.java)
      .returnResult()

    val newOrder = getOrder(order.id)

    assertThat(newOrder.orderParameters?.isMappa).isEqualTo(true)
  }

  @Test
  fun `error if isMappa is null`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/mappa/is-mappa")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(null),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    assertThat(result.responseBody!!).contains(
      ValidationError("isMappa", "Select if the device wearer is a MAPPA offender"),
    )
  }

  private fun mockRequestBody(isMappa: Boolean?): String {
    val dto = UpdateIsMappaDto(isMappa = isMappa)

    return objectMapper.writeValueAsString(dto)
  }
}
