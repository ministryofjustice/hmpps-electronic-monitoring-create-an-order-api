package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.ManageUserApiExtension.Companion.manageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsCaseload
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsUserCaseloadResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.UserDetails
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.OffsetDateTime
import java.util.*

abstract class UpdateOrderIntegrationTestBase : IntegrationTestBase() {
  abstract val testUris: List<UriTestCase>

  private fun getRequestBodyUriSpec(method: HttpMethod): WebTestClient.RequestBodyUriSpec = when (method) {
    HttpMethod.GET -> webTestClient.get()
    HttpMethod.POST -> webTestClient.post()
    HttpMethod.DELETE -> webTestClient.delete()
    else -> webTestClient.put()
  } as WebTestClient.RequestBodyUriSpec

  @Test
  fun `for each uri, it should return an error if the order was not created by the user`() {
    val order = createOrder()

    testUris.forEach { case ->
      val result = getRequestBodyUriSpec(case.httpMethod)
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
      getRequestBodyUriSpec(case.httpMethod)
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
      val result = getRequestBodyUriSpec(case.httpMethod)
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
  fun `it should update order last updated`() {
    val mockUserCohort = HmppsUserCaseloadResponse(
      "AUTH_ADM",
      true,
      "mock account",
      HmppsCaseload("ACI", "HMP ABC"),
      emptyList(),
    )
    manageUserApi.stubUserActiveCaseLoad(mockUserCohort)

    val mockUserDetails = UserDetails(
      username = "AUTH_ADM",
      active = true,
      name = "John Smith",
      authSource = "mockSource",
      userId = "ABC",
      uuid = null,
    )

    manageUserApi.stubGetUserDetails(mockUserCohort.username, mockUserDetails)
    val order = createOrder()
    val beforeCall = OffsetDateTime.now()

    testUris.forEach { case ->
      getRequestBodyUriSpec(case.httpMethod)
        .uri(case.uri.replace(":orderId", order.id.toString()))
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            case.createValidBody(),
          ),
        )
        .headers(setAuthorisation(roles = listOf("ROLE_EM_CEMO__CREATE_ORDER", "ROLE_PRISON")))
        .exchange()

      val updatedOrder = getOrder(order.id)
      Assertions.assertThat(updatedOrder.lastUpdatedBy).isEqualTo("John Smith")
      Assertions.assertThat(updatedOrder.lastUpdatedDateTime).isNotNull
      Assertions.assertThat(updatedOrder.lastUpdatedDateTime!!.isAfter(beforeCall))
    }
  }
}

data class UriTestCase(val uri: String, val createValidBody: () -> String, val httpMethod: HttpMethod = HttpMethod.PUT)
