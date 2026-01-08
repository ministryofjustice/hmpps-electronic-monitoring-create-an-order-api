package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import java.util.*

class InstallationLocationControllerTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Installation location cannot be updated by a different user`() {
    val order = createOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-location")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "location": "INSTALLATION"
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
  fun `Installation location cannot be updated for a submitted order`() {
    val order = createSubmittedOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-location")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "location": "INSTALLATION"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @ParameterizedTest(name = "it should update Installation location - {0} -> {1}")
  @MethodSource("getInstallationLocationValues")
  fun `It should map correctly store location to database`(updateValue: String, savedValue: InstallationLocationType) {
    val order = createOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-location")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "location": "$updateValue"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
    val updatedOrder = getOrder(order.id)
    assertThat(updatedOrder.installationLocation!!.location).isEqualTo(savedValue)
  }

  companion object {
    @JvmStatic
    fun getInstallationLocationValues() = listOf(
      Arguments.of("INSTALLATION", InstallationLocationType.INSTALLATION),
      Arguments.of("PRIMARY", InstallationLocationType.PRIMARY),
      Arguments.of("PRISON", InstallationLocationType.PRISON),
      Arguments.of("PROBATION_OFFICE", InstallationLocationType.PROBATION_OFFICE),
      Arguments.of("IMMIGRATION_REMOVAL_CENTRE", InstallationLocationType.IMMIGRATION_REMOVAL_CENTRE),
    )
  }
}
