package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class InterestedPartiesControllerTest : IntegrationTestBase() {

  private val mockNotifyingOrganisation = NotifyingOrganisation.HOME_OFFICE.toString()
  private val mockNotifyingOrganisationName = ""
  private val mockNotifyingOrganisationEmail = "mockNotifyingOrganisationEmail"
  private val mockResponsibleOfficerName = "mockResponsibleOfficerName"
  private val mockResponsibleOfficerPhoneNumber = "01234567890"
  private val mockResponsibleOrganisation = ResponsibleOrganisation.HOME_OFFICE.toString()
  private val mockResponsibleOrganisationRegion = ""
  private val mockResponsibleOrganisationPhoneNumber = "01234567890"
  private val mockResponsibleOrganisationEmail = "mockResponsibleOrganisationEmail"
  private val mockAddressLine1 = "mockAddressLine1"
  private val mockAddressLine2 = "mockAddressLine2"
  private val mockAddressLine3 = "mockAddressLine3"
  private val mockAddressLine4 = "mockAddressLine4"
  private val mockPostcode = "mockPostcode"

  private val mockValidRequest = """
    {
      "notifyingOrganisation": "$mockNotifyingOrganisation",
      "notifyingOrganisationName": "$mockNotifyingOrganisationName",
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
  """.trimIndent()

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Nested
  @DisplayName("PUT /api/orders/{orderId}/interested-parties")
  inner class UpdateInterestedParties {
    @Test
    fun `it should return an error if the order was not created by the user`() {
      val order = createOrder()

      webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(mockValidRequest),
        )
        .headers(setAuthorisation("AUTH_ADM_2"))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `it should return not found if the order does not exist`() {
      webTestClient.put()
        .uri("/api/orders/${UUID.randomUUID()}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(mockValidRequest),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `it should return an error if the order is in a submitted state`() {
      val order = createSubmittedOrder()

      webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(mockValidRequest),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `it should update the interested parties with valid request body`() {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(mockValidRequest),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(InterestedParties::class.java)
        .returnResult()

      val interestedParties = result.responseBody!!

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo(mockNotifyingOrganisation)
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo(mockNotifyingOrganisationName)
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
      Assertions.assertThat(
        interestedParties.responsibleOfficerPhoneNumber,
      ).isEqualTo(mockResponsibleOfficerPhoneNumber)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo(mockResponsibleOrganisation)
      Assertions.assertThat(
        interestedParties.responsibleOrganisationRegion,
      ).isEqualTo(mockResponsibleOrganisationRegion)
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
    fun `it should update interested parties with only required fields`() {
      val order = createOrder()

      val interestedParties = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "PROBATION",
              "responsibleOrganisation": "HOME_OFFICE"
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
        .responseBody!!

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo("PROBATION")
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo("")
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOfficerPhoneNumber).isEqualTo(null)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo("HOME_OFFICE")
      Assertions.assertThat(interestedParties.responsibleOrganisationRegion).isEqualTo("")
      Assertions.assertThat(
        interestedParties.responsibleOrganisationPhoneNumber,
      ).isEqualTo(null)
      Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo("")
      Assertions.assertThat(
        interestedParties.responsibleOrganisationAddress.addressType,
      ).isEqualTo(AddressType.RESPONSIBLE_ORGANISATION)
      Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine1).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine2).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine3).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOrganisationAddress.addressLine4).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOrganisationAddress.postcode).isEqualTo("")
      Assertions.assertThat(
        interestedParties.responsibleOrganisationAddress.addressUsage,
      ).isEqualTo(DeviceWearerAddressUsage.NA)
    }

    @Test
    fun `it should return an error if ro region is missing when ro is probation`() {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "PROBATION",
              "responsibleOrganisation": "PROBATION"
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
        .responseBody

      Assertions.assertThat(result).isNotNull
      Assertions.assertThat(result).hasSize(1)
      Assertions.assertThat(result).contains(
        ValidationError(
          "responsibleOrganisationRegion",
          ValidationErrors.RESPONSIBLE_ORGANISATION_REGION_REQUIRED,
        ),
      )
    }
  }
}
