package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UpdateOrderIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.UriTestCase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

class ProbationDeliveryUnitControllerTest : UpdateOrderIntegrationTestBase() {
  override val testUris: List<UriTestCase> = listOf(
    UriTestCase(uri = "/api/orders/:orderId/probation-delivery-unit", createValidBody = {
      """
            {
               "unit": "BURY_AND_ROCHDALE"
            }
      """.trimIndent()
    }, httpMethod = HttpMethod.PUT),
  )
  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Probation Delivery Unit can be updated with a null unit name`() {
    val order =
      createOrderWithResponsibleOrganisation(
        ResponsibleOrganisation.PROBATION,
        ProbationServiceRegion.GREATER_MANCHESTER,
      )

    webTestClient.put()
      .uri("/api/orders/${order.id}/probation-delivery-unit")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "unit": null
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
  fun `Probation Delivery Unit can be updated with a valid unit name`() {
    val order =
      createOrderWithResponsibleOrganisation(
        ResponsibleOrganisation.PROBATION,
        ProbationServiceRegion.GREATER_MANCHESTER,
      )

    webTestClient.put()
      .uri("/api/orders/${order.id}/probation-delivery-unit")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "unit": "BURY_AND_ROCHDALE"
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
  fun `Probation Delivery Unit cannot be updated when responsible organisation is not PROBATION`() {
    val order =
      createOrderWithResponsibleOrganisation(
        ResponsibleOrganisation.HOME_OFFICE,
        ProbationServiceRegion.GREATER_MANCHESTER,
      )
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/probation-delivery-unit")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "unit": "BURY_AND_ROCHDALE"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(1)

    Assertions.assertThat(result.responseBody!!).contains(
      ErrorResponse(
        status = BAD_REQUEST,
        developerMessage = "Validation failure: Responsible organisation must be Probation",
        userMessage = "Responsible organisation must be Probation",
      ),
    )
  }

  @Test
  fun `Probation Delivery Unit cannot be updated when unit is not in probation region`() {
    val order = createOrderWithResponsibleOrganisation(ResponsibleOrganisation.PROBATION, ProbationServiceRegion.LONDON)
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/probation-delivery-unit")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "unit": "BURY_AND_ROCHDALE"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(1)

    Assertions.assertThat(result.responseBody!!).contains(
      ErrorResponse(
        status = BAD_REQUEST,
        developerMessage = "Validation failure: Select probation delivery unit within given probation region",
        userMessage = "Select probation delivery unit within given probation region",
      ),
    )
  }

  private fun createOrderWithResponsibleOrganisation(
    responsibleOrganisation: ResponsibleOrganisation,
    probationServiceRegion: ProbationServiceRegion,
  ): Order {
    val order = createStoredOrder()
    order.interestedParties = InterestedParties(
      versionId = order.versions.first().id,
      responsibleOfficerName = "John Smith",
      responsibleOfficerPhoneNumber = "07401111111",
      responsibleOrganisation = responsibleOrganisation.name,
      responsibleOrganisationRegion = probationServiceRegion.name,
      responsibleOrganisationEmail = "abc@def.com",
      notifyingOrganisation = "PRISON",
      notifyingOrganisationName = "WAYLAND_PRISON",
      notifyingOrganisationEmail = "",
    )
    return repo.save(order)
  }
}
