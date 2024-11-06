package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class InterestedPartiesControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var orderRepo: OrderRepository

  private val mockNotifyingOrganisationEmail: String = "mockNotifyingOrganisationEmail"
  private val mockResponsibleOfficerName: String = "mockResponsibleOfficerName"
  private val mockResponsibleOfficerPhoneNumber: String = "01234567890"
  private val mockResponsibleOrganisation: String = ResponsibleOrganisation.PROBATION.toString()
  private val mockResponsibleOrganisationRegion: String = "mockResponsibleOrganisationRegion"
  private val mockResponsibleOrganisationPhoneNumber: String = "01234567890"
  private val mockResponsibleOrganisationEmail: String = "mockResponsibleOrganisationEmail"
  private val mockAddressLine1: String = "mockAddressLine1"
  private val mockAddressLine2: String = "mockAddressLine2"
  private val mockAddressLine3: String = "mockAddressLine3"
  private val mockAddressLine4: String = "mockAddressLine4"
  private val mockPostcode: String = "mockPostcode"

  @BeforeEach
  fun setup() {
    orderRepo.deleteAll()
  }

  @Test
  fun `Interested Parties details for an order created by a different user are not update-able`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/interested-parties")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "notifyingOrganisationEmail": "",
              "responsibleOfficerName": "",
              "responsibleOfficerPhoneNumber": null,
              "responsibleOrganisation": null,
              "responsibleOrganisationRegion": "",
              "responsibleOrganisationPhoneNumber": null,
              "responsibleOrganisationEmail": "",
              "responsibleOrganisationAddressLine1": "$mockAddressLine1",
              "responsibleOrganisationAddressLine2": "$mockAddressLine2",
              "responsibleOrganisationAddressLine3": "$mockAddressLine3",
              "responsibleOrganisationAddressLine4": "$mockAddressLine4",
              "responsibleOrganisationAddressPostcode": "$mockPostcode"
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
  fun `Interested parties details for a non-existent order are not update-able`() {
    webTestClient.put()
      .uri("/api/orders/${UUID.randomUUID()}/interested-parties")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "notifyingOrganisationEmail": "",
              "responsibleOfficerName": "",
              "responsibleOfficerPhoneNumber": null,
              "responsibleOrganisation": null,
              "responsibleOrganisationRegion": "",
              "responsibleOrganisationPhoneNumber": null,
              "responsibleOrganisationEmail": "",
              "responsibleOrganisationAddressLine1": "$mockAddressLine1",
              "responsibleOrganisationAddressLine2": "$mockAddressLine2",
              "responsibleOrganisationAddressLine3": "$mockAddressLine3",
              "responsibleOrganisationAddressLine4": "$mockAddressLine4",
              "responsibleOrganisationAddressPostcode": "$mockPostcode"
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
  fun `Interested parties details for a submitted order are not update-able`() {
    val order = createOrder()

    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)

    webTestClient.put()
      .uri("/api/orders/${order.id}/interested-parties")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "notifyingOrganisationEmail": "",
              "responsibleOfficerName": "",
              "responsibleOfficerPhoneNumber": null,
              "responsibleOrganisation": null,
              "responsibleOrganisationRegion": "",
              "responsibleOrganisationPhoneNumber": null,
              "responsibleOrganisationEmail": "",
              "responsibleOrganisationAddressLine1": "$mockAddressLine1",
              "responsibleOrganisationAddressLine2": "$mockAddressLine2",
              "responsibleOrganisationAddressLine3": "$mockAddressLine3",
              "responsibleOrganisationAddressLine4": "$mockAddressLine4",
              "responsibleOrganisationAddressPostcode": "$mockPostcode"
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
  fun `Interested parties details can be updated`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/interested-parties")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "notifyingOrganisationEmail": "$mockNotifyingOrganisationEmail",
              "responsibleOfficerName": "$mockResponsibleOfficerName",
              "responsibleOfficerPhoneNumber": "$mockResponsibleOfficerPhoneNumber",
              "responsibleOrganisation": "$mockResponsibleOrganisation",
              "responsibleOrganisationRegion": "$mockResponsibleOrganisationRegion",
              "responsibleOrganisationPhoneNumber": "$mockResponsibleOrganisationPhoneNumber",
              "responsibleOrganisationEmail": "$mockResponsibleOrganisationEmail",
              "responsibleOrganisationAddressLine1": "$mockAddressLine1",
              "responsibleOrganisationAddressLine2": "$mockAddressLine2",
              "responsibleOrganisationAddressLine3": "$mockAddressLine3",
              "responsibleOrganisationAddressLine4": "$mockAddressLine4",
              "responsibleOrganisationAddressPostcode": "$mockPostcode"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(InterestedParties::class.java)
      .returnResult()

    val interestedParties = result.responseBody!!

    Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
    Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
    Assertions.assertThat(interestedParties.responsibleOfficerPhoneNumber).isEqualTo(mockResponsibleOfficerPhoneNumber)
    Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo(mockResponsibleOrganisation)
    Assertions.assertThat(interestedParties.responsibleOrganisationRegion).isEqualTo(mockResponsibleOrganisationRegion)
    Assertions.assertThat(
      interestedParties.responsibleOrganisationPhoneNumber,
    ).isEqualTo(mockResponsibleOrganisationPhoneNumber)
    Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo(mockResponsibleOrganisationEmail)
    Assertions.assertThat(
      interestedParties.responsibleOrganisationAddress.addressType,
    ).isEqualTo(AddressType.RESPONSIBLE_ORGANISATION)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine1).isEqualTo(mockAddressLine1)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine2).isEqualTo(mockAddressLine2)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine3).isEqualTo(mockAddressLine3)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine4).isEqualTo(mockAddressLine4)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.postcode).isEqualTo(mockPostcode)
    Assertions.assertThat(
      interestedParties.responsibleOrganisationAddress.addressUsage,
    ).isEqualTo(DeviceWearerAddressUsage.NA)
  }

  @Test
  fun `Interested parties details can be updated with a null responsible organisation`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/interested-parties")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "notifyingOrganisationEmail": "$mockNotifyingOrganisationEmail",
              "responsibleOfficerName": "$mockResponsibleOfficerName",
              "responsibleOfficerPhoneNumber": "$mockResponsibleOfficerPhoneNumber",
              "responsibleOrganisation": null,
              "responsibleOrganisationRegion": "$mockResponsibleOrganisationRegion",
              "responsibleOrganisationPhoneNumber": "$mockResponsibleOrganisationPhoneNumber",
              "responsibleOrganisationEmail": "$mockResponsibleOrganisationEmail",
              "responsibleOrganisationAddressLine1": "$mockAddressLine1",
              "responsibleOrganisationAddressLine2": "$mockAddressLine2",
              "responsibleOrganisationAddressLine3": "$mockAddressLine3",
              "responsibleOrganisationAddressLine4": "$mockAddressLine4",
              "responsibleOrganisationAddressPostcode": "$mockPostcode"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(InterestedParties::class.java)
      .returnResult()

    val interestedParties = result.responseBody!!

    Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
    Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
    Assertions.assertThat(interestedParties.responsibleOfficerPhoneNumber).isEqualTo(mockResponsibleOfficerPhoneNumber)
    Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo(null)
    Assertions.assertThat(interestedParties.responsibleOrganisationRegion).isEqualTo(mockResponsibleOrganisationRegion)
    Assertions.assertThat(
      interestedParties.responsibleOrganisationPhoneNumber,
    ).isEqualTo(mockResponsibleOrganisationPhoneNumber)
    Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo(mockResponsibleOrganisationEmail)
    Assertions.assertThat(
      interestedParties.responsibleOrganisationAddress.addressType,
    ).isEqualTo(AddressType.RESPONSIBLE_ORGANISATION)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine1).isEqualTo(mockAddressLine1)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine2).isEqualTo(mockAddressLine2)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine3).isEqualTo(mockAddressLine3)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine4).isEqualTo(mockAddressLine4)
    Assertions.assertThat(interestedParties.responsibleOrganisationAddress.postcode).isEqualTo(mockPostcode)
    Assertions.assertThat(
      interestedParties.responsibleOrganisationAddress.addressUsage,
    ).isEqualTo(DeviceWearerAddressUsage.NA)
  }

  @Test
  fun `Interested parties details mandatory fields must contain valid values`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/interested-parties")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "notifyingOrganisationEmail": "",
              "responsibleOfficerName": "",
              "responsibleOfficerPhoneNumber": null,
              "responsibleOrganisation": null,
              "responsibleOrganisationRegion": "",
              "responsibleOrganisationPhoneNumber": null,
              "responsibleOrganisationEmail": "",
              "responsibleOrganisationAddressLine1": "",
              "responsibleOrganisationAddressLine2": "",
              "responsibleOrganisationAddressLine3": "",
              "responsibleOrganisationAddressLine4": "",
              "responsibleOrganisationAddressPostcode": ""
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(3)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("responsibleOrganisationAddressLine1", "Address line 1 is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("responsibleOrganisationAddressLine2", "Address line 2 is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("responsibleOrganisationAddressPostcode", "Postcode is required"),
    )
  }
}
