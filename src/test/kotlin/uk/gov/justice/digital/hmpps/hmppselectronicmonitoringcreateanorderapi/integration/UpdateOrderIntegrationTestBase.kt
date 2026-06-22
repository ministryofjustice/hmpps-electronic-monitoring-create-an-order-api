package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

abstract class UpdateOrderIntegrationTestBase : IntegrationTestBase() {
  abstract val testUris: List<UriTestCase>

  @Test
  fun `for each uri, it should return an error if the order was not created by the user`() {
    val order = createOrder()

    testUris.forEach { case ->
      val result = webTestClient.put()
        .uri(case.uri.replace(":orderId", order.id.toString()))
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            case.createValidBody(),
          ),
        )
        .headers(setAuthorisation("Another user"))
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
  }

  @Test
  fun `it should return not found if the order does not exist`() {
    this.testUris.forEach { case ->
      webTestClient.put()
        .uri(case.uri.replace(":orderId", UUID.randomUUID().toString()))
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            case.createValidBody(),
          ),
        )
        .headers(setAuthorisation("Another user"))
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }

  @Test
  fun `it should return an error if the order is in a submitted state`() {
    val order = createSubmittedOrder()
    this.testUris.forEach { case ->
      val result = webTestClient.put()
        .uri(case.uri.replace(":orderId", order.id.toString()))
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            case.createValidBody(),
          ),
        )
        .headers(setAuthorisation("Another user"))
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
  }
}

data class UriTestCase(val uri: String, val createValidBody: () -> String)
