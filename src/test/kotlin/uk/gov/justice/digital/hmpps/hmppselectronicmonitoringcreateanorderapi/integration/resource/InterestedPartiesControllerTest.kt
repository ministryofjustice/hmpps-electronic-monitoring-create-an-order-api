package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
    fun `it should return a validation error if mandatory fields are missing`() {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {}
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
      Assertions.assertThat(result).hasSize(2)
      Assertions.assertThat(result).contains(
        ValidationError(
          "responsibleOrganisation",
          ValidationErrors.RESPONSIBLE_ORGANISATION_REQUIRED,
        ),
      )
      Assertions.assertThat(result).contains(
        ValidationError(
          "notifyingOrganisation",
          ValidationErrors.NOTIFYING_ORGANISATION_REQUIRED,
        ),
      )
    }

    @ParameterizedTest(name = "it should return a validation error for region={0} when responsible org is probation")
    @ValueSource(strings = ["", "NORTH_EAST_AND_CUMBRIA"])
    fun `it should return a validation error for invalid regions when responsible organisation is probation`(
      value: String,
    ) {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "PROBATION",
              "responsibleOrganisation": "PROBATION",
              "responsibleOrganisationRegion": "$value"
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

    @Test
    fun `it should accept a probation region when responsible organisation is probation`() {
      val order = createOrder()

      val interestedParties = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "PROBATION",
              "responsibleOrganisation": "PROBATION",
              "responsibleOrganisationRegion": "YORKSHIRE_AND_THE_HUMBER"
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
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo("PROBATION")
      Assertions.assertThat(interestedParties.responsibleOrganisationRegion).isEqualTo("YORKSHIRE_AND_THE_HUMBER")
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

    @ParameterizedTest(name = "it should return a validation error for region={0} when responsible org is YJS")
    @ValueSource(strings = ["", "KENT_SURREY_SUSSEX"])
    fun `it should return a validation error for invalid regions when responsible organisation is YJS`(value: String) {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "PROBATION",
              "responsibleOrganisation": "YJS",
              "responsibleOrganisationRegion": "$value"
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

    @Test
    fun `it should accept a yjs region when responsible organisation is YJS`() {
      val order = createOrder()

      val interestedParties = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "PROBATION",
              "responsibleOrganisation": "YJS",
              "responsibleOrganisationRegion": "NORTH_EAST_AND_CUMBRIA"
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
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo("YJS")
      Assertions.assertThat(interestedParties.responsibleOrganisationRegion).isEqualTo("NORTH_EAST_AND_CUMBRIA")
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

    @ParameterizedTest(name = "it should return a validation error for name={0} when notifying org is PRISON")
    @ValueSource(strings = ["", "YORK_CROWN_COURT", "BELMARSH_MAGISTRATES_COURT"])
    fun `it should return a validation error for invalid names when notifying organisation is PRISON`(value: String) {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "PRISON",
              "notifyingOrganisationName": "$value",
              "responsibleOrganisation": "FIELD_MONITORING_SERVICE"
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
          "notifyingOrganisationName",
          ValidationErrors.NOTIFYING_ORGANISATION_NAME_REQUIRED,
        ),
      )
    }

    @Test
    fun `it should accept a prison name when notifying organisation is PRISON`() {
      val order = createOrder()

      val interestedParties = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "PRISON",
              "notifyingOrganisationName": "BELMARSH_PRISON",
              "responsibleOrganisation": "FIELD_MONITORING_SERVICE"
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

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo("PRISON")
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo("BELMARSH_PRISON")
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOfficerPhoneNumber).isEqualTo(null)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo("FIELD_MONITORING_SERVICE")
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

    @ParameterizedTest(name = "it should return a validation error for name={0} when notifying org is CROWN_COURT")
    @ValueSource(strings = ["", "BELMARSH_PRISON", "BELMARSH_MAGISTRATES_COURT"])
    fun `it should return a validation error for invalid names when notifying organisation is CROWN_COURT`(
      value: String,
    ) {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "CROWN_COURT",
              "notifyingOrganisationName": "$value",
              "responsibleOrganisation": "FIELD_MONITORING_SERVICE"
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
          "notifyingOrganisationName",
          ValidationErrors.NOTIFYING_ORGANISATION_NAME_REQUIRED,
        ),
      )
    }

    @Test
    fun `it should accept a crown court name when notifying organisation is CROWN_COURT`() {
      val order = createOrder()

      val interestedParties = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "CROWN_COURT",
              "notifyingOrganisationName": "YORK_CROWN_COURT",
              "responsibleOrganisation": "FIELD_MONITORING_SERVICE"
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

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo("CROWN_COURT")
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo("YORK_CROWN_COURT")
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOfficerPhoneNumber).isEqualTo(null)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo("FIELD_MONITORING_SERVICE")
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

    @ParameterizedTest(name = "it should return an error for name={0} when notifying org is MAGISTRATES_COURT")
    @ValueSource(strings = ["", "BELMARSH_PRISON", "CARDIFF_CROWN_COURT"])
    fun `it should return a validation error for invalid names when notifying organisation is MAGISTRATES_COURT`(
      value: String,
    ) {
      val order = createOrder()

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "MAGISTRATES_COURT",
              "notifyingOrganisationName": "$value",
              "responsibleOrganisation": "FIELD_MONITORING_SERVICE"
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
          "notifyingOrganisationName",
          ValidationErrors.NOTIFYING_ORGANISATION_NAME_REQUIRED,
        ),
      )
    }

    @Test
    fun `it should accept a magistrate court name when notifying organisation is MAGISTRATES_COURT`() {
      val order = createOrder()

      val interestedParties = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "notifyingOrganisation": "MAGISTRATES_COURT",
              "notifyingOrganisationName": "BRADFORD_AND_KEIGHLEY_MAGISTRATES_COURT",
              "responsibleOrganisation": "FIELD_MONITORING_SERVICE"
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

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo("MAGISTRATES_COURT")
      Assertions.assertThat(
        interestedParties.notifyingOrganisationName,
      ).isEqualTo("BRADFORD_AND_KEIGHLEY_MAGISTRATES_COURT")
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo("")
      Assertions.assertThat(interestedParties.responsibleOfficerPhoneNumber).isEqualTo(null)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo("FIELD_MONITORING_SERVICE")
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
  }
}
