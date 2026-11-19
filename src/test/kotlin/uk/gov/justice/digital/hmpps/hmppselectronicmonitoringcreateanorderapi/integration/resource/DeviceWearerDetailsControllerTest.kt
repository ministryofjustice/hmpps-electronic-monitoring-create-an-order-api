package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.CorePersonRecordApiExtension.Companion.corePersonRecordApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.CreateAndVaryALicenceApiExtension.Companion.createAndVaryALicenceApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.GetCorePersonDetailsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.UUID

class DeviceWearerDetailsControllerTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `returns device wearer details looked up from the order's notifying organisation`() {
    val order = createOrder()
    val storedOrder = repo.findById(order.id).orElseThrow()
    storedOrder.interestedParties = InterestedParties(
      versionId = storedOrder.versionId,
      notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name,
    )
    repo.save(storedOrder)

    corePersonRecordApi.stubGetPrisonerDetails(
      "A1234BC",
      """
        {
          "firstName": "Bob",
          "lastName": "Builder",
          "dateOfBirth": "1990-08-21",
          "nationalities": []
        }
      """.trimIndent(),
    )

    val result = webTestClient.get()
      .uri("/api/orders/${order.id}/device-wearer-details?organisationSearchId=A1234BC")
      .headers(setAuthorisation(testUser))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(GetCorePersonDetailsResponse::class.java)
      .returnResult()
      .responseBody!!

    assertThat(result.firstName).isEqualTo("Bob")
    assertThat(result.lastName).isEqualTo("Builder")
    assertThat(result.organisationSearchId).isEqualTo("A1234BC")
  }

  @Test
  fun `returns not found when the order does not belong to the current user`() {
    val order = createOrder()
    val storedOrder = repo.findById(order.id).orElseThrow()
    storedOrder.interestedParties = InterestedParties(
      versionId = storedOrder.versionId,
      notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name,
    )
    repo.save(storedOrder)

    webTestClient.get()
      .uri("/api/orders/${order.id}/device-wearer-details?organisationSearchId=A1234BC")
      .headers(setAuthorisation("someone-else"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `returns not found when the order does not exist`() {
    webTestClient.get()
      .uri("/api/orders/${UUID.randomUUID()}/device-wearer-details?organisationSearchId=A1234BC")
      .headers(setAuthorisation(testUser))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `stores device wearer details on the order`() {
    val order = createOrder()
    val storedOrder = repo.findById(order.id).orElseThrow()
    storedOrder.interestedParties = InterestedParties(
      versionId = storedOrder.versionId,
      notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name,
    )
    repo.save(storedOrder)

    corePersonRecordApi.stubGetPrisonerDetails(
      "A1234BC",
      """
        {
          "firstName": "Bob",
          "lastName": "Builder",
          "dateOfBirth": "1990-08-21",
          "nationalities": []
        }
      """.trimIndent(),
    )

    webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "organisationSearchId": "A1234BC"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation(testUser))
      .exchange()
      .expectStatus()
      .isNoContent

    val updatedOrder = repo.findById(order.id).orElseThrow()
    assertThat(updatedOrder.deviceWearer?.firstName).isEqualTo("Bob")
  }

  @Test
  fun `returns bad request when organisationSearchId is blank`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "organisationSearchId": ""
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation(testUser))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    assertThat(result.responseBody).isNotNull
    assertThat(result.responseBody!!).contains(
      ValidationError("organisationSearchId", "organisationSearchId must not be blank"),
    )
  }

  @Test
  fun `returns not found when storing details against an order owned by another user`() {
    val order = createOrder()
    val storedOrder = repo.findById(order.id).orElseThrow()
    storedOrder.interestedParties = InterestedParties(
      versionId = storedOrder.versionId,
      notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name,
    )
    repo.save(storedOrder)

    webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "organisationSearchId": "A1234BC"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("someone-else"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `imports licence conditions from CVL when storing device wearer details`() {
    val order = createPrisonOrder()
    stubPrisonerAndLicence("A1234BC")

    putDeviceWearerDetails(order.id, "A1234BC")

    val updatedOrder = repo.findById(order.id).orElseThrow()
    assertThat(updatedOrder.deviceWearer?.firstName).isEqualTo("Bob")
    assertThat(updatedOrder.monitoringConditions?.trail).isTrue()
    assertThat(updatedOrder.monitoringConditionsTrail).isNotNull
  }

  @Test
  fun `stores device wearer details when CVL has no licence for the person`() {
    val order = createPrisonOrder()
    stubPrisoner("A1234BC")
    createAndVaryALicenceApi.stubGetLicenceByPrisonNumber("A1234BC", "[]")

    putDeviceWearerDetails(order.id, "A1234BC")

    val updatedOrder = repo.findById(order.id).orElseThrow()
    assertThat(updatedOrder.deviceWearer?.firstName).isEqualTo("Bob")
    assertThat(updatedOrder.monitoringConditionsTrail).isNull()
  }

  @Test
  fun `stores device wearer details when CVL responds with a server error`() {
    val order = createPrisonOrder()
    stubPrisoner("A1234BC")
    createAndVaryALicenceApi.stubGetLicenceByPrisonNumberError("A1234BC", 500)

    putDeviceWearerDetails(order.id, "A1234BC")

    val updatedOrder = repo.findById(order.id).orElseThrow()
    assertThat(updatedOrder.deviceWearer?.firstName).isEqualTo("Bob")
    assertThat(updatedOrder.monitoringConditionsTrail).isNull()
  }

  @Test
  fun `re-importing licence conditions does not duplicate or discard existing conditions`() {
    val order = createPrisonOrder()
    stubPrisonerAndLicence("A1234BC")

    putDeviceWearerDetails(order.id, "A1234BC")
    val first = repo.findById(order.id).orElseThrow()
    val trailId = first.monitoringConditionsTrail!!.id
    val zoneCount = first.enforcementZoneConditions.size

    putDeviceWearerDetails(order.id, "A1234BC")

    val second = repo.findById(order.id).orElseThrow()
    assertThat(second.monitoringConditionsTrail!!.id).isEqualTo(trailId)
    assertThat(second.enforcementZoneConditions).hasSize(zoneCount)
    assertThat(second.monitoringConditions?.trail).isTrue()
  }

  private fun createPrisonOrder(): OrderDto {
    val order = createOrder()
    val storedOrder = repo.findById(order.id).orElseThrow()
    storedOrder.interestedParties = InterestedParties(
      versionId = storedOrder.versionId,
      notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name,
    )
    repo.save(storedOrder)
    return order
  }

  private fun stubPrisoner(prisonNumber: String) {
    corePersonRecordApi.stubGetPrisonerDetails(
      prisonNumber,
      """{"firstName": "Bob", "lastName": "Builder", "dateOfBirth": "1990-08-21", "nationalities": []}""",
    )
  }

  private fun stubPrisonerAndLicence(prisonNumber: String) {
    stubPrisoner(prisonNumber)
    createAndVaryALicenceApi.stubGetLicenceByPrisonNumber(
      prisonNumber,
      """[{"id": "1", "statusCode": "APPROVED", "approvedDateTime": "2026-11-01T10:00:00"}]""",
    )
    createAndVaryALicenceApi.stubGetLicenceConditions(
      "1",
      this::class.java.getResource("/fixtures/cvl/licenceConditionsWithEM.json")!!.readText(),
    )
  }

  private fun putDeviceWearerDetails(orderId: UUID, organisationSearchId: String) {
    webTestClient.put()
      .uri("/api/orders/$orderId/device-wearer-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue("""{"organisationSearchId": "$organisationSearchId"}"""))
      .headers(setAuthorisation(testUser))
      .exchange()
      .expectStatus()
      .isNoContent
  }
}
