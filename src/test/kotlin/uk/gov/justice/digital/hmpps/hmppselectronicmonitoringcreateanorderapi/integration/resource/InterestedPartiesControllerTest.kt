package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class InterestedPartiesControllerTest : IntegrationTestBase() {

  private val mockNotifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE.toString()
  private val mockNotifyingOrganisationName = ""
  private val mockNotifyingOrganisationEmail = "mockNotifyingOrganisationEmail"
  private val mockResponsibleOfficerName = "mockResponsibleOfficerName"
  private val mockResponsibleOfficerPhoneNumber = "01234567890"
  private val mockResponsibleOrganisation = ResponsibleOrganisation.HOME_OFFICE.toString()
  private val mockResponsibleOrganisationRegion = ""
  private val mockResponsibleOrganisationEmail = "mockResponsibleOrganisationEmail"

  private fun buildMockRequest(
    notifyingOrganisation: String? = mockNotifyingOrganisation,
    notifyingOrganisationName: String? = mockNotifyingOrganisationName,
    notifyingOrganisationEmail: String? = mockNotifyingOrganisationEmail,
    responsibleOfficerName: String? = mockResponsibleOfficerName,
    responsibleOfficerPhoneNumber: String? = mockResponsibleOfficerPhoneNumber,
    responsibleOrganisation: String? = mockResponsibleOrganisation,
    responsibleOrganisationRegion: String? = mockResponsibleOrganisationRegion,
    responsibleOrganisationEmail: String? = mockResponsibleOrganisationEmail,
  ): String = """
    {
      "notifyingOrganisation": "$notifyingOrganisation",
      "notifyingOrganisationName": "$notifyingOrganisationName",
      "notifyingOrganisationEmail": "$notifyingOrganisationEmail",
      "responsibleOfficerName": "$responsibleOfficerName",
      "responsibleOfficerPhoneNumber": "$responsibleOfficerPhoneNumber",
      "responsibleOrganisation": "$responsibleOrganisation",
      "responsibleOrganisationRegion": "$responsibleOrganisationRegion",
      "responsibleOrganisationEmail": "$responsibleOrganisationEmail"
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
          BodyInserters.fromValue(buildMockRequest()),
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
          BodyInserters.fromValue(buildMockRequest()),
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
          BodyInserters.fromValue(buildMockRequest()),
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
          BodyInserters.fromValue(buildMockRequest()),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(InterestedParties::class.java)
        .returnResult()

      val interestedParties = result.responseBody!!

      assertThat(interestedParties.notifyingOrganisation).isEqualTo(mockNotifyingOrganisation)

      assertThat(interestedParties.notifyingOrganisationName).isEqualTo(mockNotifyingOrganisationName)

      assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)

      assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)

      assertThat(
        interestedParties.responsibleOfficerPhoneNumber,
      ).isEqualTo(mockResponsibleOfficerPhoneNumber)

      assertThat(interestedParties.responsibleOrganisation).isEqualTo(mockResponsibleOrganisation)

      assertThat(
        interestedParties.responsibleOrganisationRegion,
      ).isEqualTo(mockResponsibleOrganisationRegion)

      assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo(mockResponsibleOrganisationEmail)
    }

    @Test
    fun `it should update interested parties with only required fields`() {
      val order = createOrder()

      val interestedParties = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            buildMockRequest(
              responsibleOrganisationEmail = "",
            ),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(InterestedParties::class.java)
        .returnResult()
        .responseBody!!

      assertThat(interestedParties.notifyingOrganisation).isEqualTo(mockNotifyingOrganisation)
      assertThat(interestedParties.notifyingOrganisationName).isEqualTo("")
      assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
      assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
      assertThat(
        interestedParties.responsibleOfficerPhoneNumber,
      ).isEqualTo(mockResponsibleOfficerPhoneNumber)
      assertThat(interestedParties.responsibleOrganisation).isEqualTo(mockResponsibleOrganisation)
      assertThat(
        interestedParties.responsibleOrganisationRegion,
      ).isEqualTo(mockResponsibleOrganisationRegion)
      assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo("")
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

      assertThat(result).isNotNull
      assertThat(result?.sortedBy { it.field }).isEqualTo(
        listOf(
          ValidationError(
            "notifyingOrganisation",
            ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_REQUIRED,
          ),
          ValidationError(
            "notifyingOrganisationEmail",
            ValidationErrors.InterestedParties.TEAM_EMAIL_REQUIRED,
          ),
          ValidationError(
            "responsibleOfficerName",
            ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_FULL_NAME_REQUIRED,
          ),
          ValidationError(
            "responsibleOfficerPhoneNumber",
            ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_TELEPHONE_NUMBER_REQUIRED,
          ),
          ValidationError(
            "responsibleOrganisation",
            ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_REQUIRED,
          ),
        ),
      )
    }

    @Nested
    inner class ResponsibleOrganisation {
      @ParameterizedTest(name = "it should return a validation error for region={0} when responsible org is probation")
      @ValueSource(strings = ["", "NORTH_EAST_AND_CUMBRIA"])
      fun `it should return a validation error for invalid region when responsible organisation is probation`(
        value: String,
      ) {
        val order = createOrder()

        val result = webTestClient.put()
          .uri("/api/orders/${order.id}/interested-parties")
          .contentType(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              buildMockRequest(
                notifyingOrganisation = "PROBATION",
                responsibleOrganisation = "PROBATION",
                responsibleOrganisationRegion = value,
              ),
            ),
          )
          .headers(setAuthorisation("AUTH_ADM"))
          .exchange()
          .expectStatus()
          .isBadRequest
          .expectBodyList(ValidationError::class.java)
          .returnResult()
          .responseBody

        assertThat(result).isNotNull
        assertThat(result?.sortedBy { it.field }).isEqualTo(
          listOf(
            ValidationError(
              "responsibleOrganisationRegion",
              ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_REGION_REQUIRED,
            ),
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
              buildMockRequest(
                responsibleOrganisation = "PROBATION",
                responsibleOrganisationRegion = "YORKSHIRE_AND_THE_HUMBER",
              ),
            ),
          )
          .headers(setAuthorisation("AUTH_ADM"))
          .exchange()
          .expectStatus()
          .isOk
          .expectBody(InterestedParties::class.java)
          .returnResult()
          .responseBody!!

        assertThat(interestedParties.notifyingOrganisation).isEqualTo(mockNotifyingOrganisation)
        assertThat(interestedParties.notifyingOrganisationName).isEqualTo(mockNotifyingOrganisationName)
        assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
        assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
        assertThat(
          interestedParties.responsibleOfficerPhoneNumber,
        ).isEqualTo(mockResponsibleOfficerPhoneNumber)
        assertThat(interestedParties.responsibleOrganisation).isEqualTo("PROBATION")
        assertThat(interestedParties.responsibleOrganisationRegion).isEqualTo("YORKSHIRE_AND_THE_HUMBER")
        assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo(mockResponsibleOrganisationEmail)
      }

      @ParameterizedTest(name = "it should return a validation error for region={0} when responsible org is YJS")
      @ValueSource(strings = ["", "KENT_SURREY_SUSSEX"])
      fun `it should return a validation error for invalid regions when responsible organisation is YJS`(
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

        assertThat(result).isNotNull
        assertThat(result?.sortedBy { it.field }).isEqualTo(
          listOf(
            ValidationError(
              "notifyingOrganisationEmail",
              ValidationErrors.InterestedParties.TEAM_EMAIL_REQUIRED,
            ),
            ValidationError(
              "responsibleOfficerName",
              ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_FULL_NAME_REQUIRED,
            ),
            ValidationError(
              "responsibleOfficerPhoneNumber",
              ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_TELEPHONE_NUMBER_REQUIRED,
            ),
            ValidationError(
              "responsibleOrganisationRegion",
              ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_REGION_REQUIRED,
            ),
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
              "responsibleOrganisationRegion": "NORTH_EAST_AND_CUMBRIA",
              "responsibleOfficerName": "Jeff Testberg",
              "notifyingOrganisationEmail": "test@test.com",
              "responsibleOfficerPhoneNumber": "01234567890"
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

        assertThat(interestedParties.notifyingOrganisation).isEqualTo("PROBATION")
        assertThat(interestedParties.notifyingOrganisationName).isEqualTo("")
        assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo("test@test.com")
        assertThat(interestedParties.responsibleOfficerName).isEqualTo("Jeff Testberg")
        assertThat(interestedParties.responsibleOfficerPhoneNumber).isEqualTo("01234567890")
        assertThat(interestedParties.responsibleOrganisation).isEqualTo("YJS")
        assertThat(interestedParties.responsibleOrganisationRegion).isEqualTo("NORTH_EAST_AND_CUMBRIA")
        assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo("")
      }
    }

    @Nested
    inner class NotifyingOrganisation {
      @ParameterizedTest(
        name = "it should return a validation error for name={0} when notifying org is CIVIL_COUNTY_COURT",
      )
      @ValueSource(strings = ["", "BELMARSH_PRISON", "BELMARSH_MAGISTRATES_COURT"])
      fun `it should return a validation error for invalid names when notifying organisation is CIVIL_COUNTY_COURT`(
        value: String,
      ) {
        assertInvalidNotifyingOrg("CIVIL_COUNTY_COURT", value)
      }

      @Test
      fun `it should accept a civil county court name when notifying organisation is CIVIL_COUNTY_COURT`() {
        assertValidNotifyingOrg("CIVIL_COUNTY_COURT", "THANET_COUNTY_AND_CIVIL_COURT")
      }

      @ParameterizedTest(name = "it should return a validation error for name={0} when notifying org is CROWN_COURT")
      @ValueSource(strings = ["", "BELMARSH_PRISON", "BELMARSH_MAGISTRATES_COURT"])
      fun `it should return a validation error for invalid names when notifying organisation is CROWN_COURT`(
        value: String,
      ) {
        assertInvalidNotifyingOrg("CROWN_COURT", value)
      }

      @Test
      fun `it should accept a crown court name when notifying organisation is CROWN_COURT`() {
        assertValidNotifyingOrg("CROWN_COURT", "YORK_CROWN_COURT")
      }

      @ParameterizedTest(name = "it should return a validation error for name={0} when notifying org is FAMILY_COURT")
      @ValueSource(strings = ["", "BELMARSH_PRISON", "BELMARSH_MAGISTRATES_COURT"])
      fun `it should return a validation error for invalid names when notifying organisation is FAMILY_COURT`(
        value: String,
      ) {
        assertInvalidNotifyingOrg("FAMILY_COURT", value)
      }

      @Test
      fun `it should accept a family court name when notifying organisation is FAMILY_COURT`() {
        assertValidNotifyingOrg("FAMILY_COURT", "MERTHYR_FAMILY_COURT")
      }

      @ParameterizedTest(name = "it should return an error for name={0} when notifying org is MAGISTRATES_COURT")
      @ValueSource(strings = ["", "BELMARSH_PRISON", "CARDIFF_CROWN_COURT"])
      fun `it should return a validation error for invalid names when notifying organisation is MAGISTRATES_COURT`(
        value: String,
      ) {
        assertInvalidNotifyingOrg("MAGISTRATES_COURT", value)
      }

      @Test
      fun `it should accept a magistrates court name when notifying organisation is MAGISTRATES_COURT`() {
        assertValidNotifyingOrg("MAGISTRATES_COURT", "BRADFORD_AND_KEIGHLEY_MAGISTRATES_COURT")
      }

      @ParameterizedTest(name = "it should return a validation error for name={0} when notifying org is MILITARY_COURT")
      @ValueSource(strings = ["", "BELMARSH_PRISON", "BELMARSH_MAGISTRATES_COURT"])
      fun `it should return a validation error for invalid names when notifying organisation is MILITARY_COURT`(
        value: String,
      ) {
        assertInvalidNotifyingOrg("MILITARY_COURT", value)
      }

      @Test
      fun `it should accept a military court name when notifying organisation is MILITARY_COURT`() {
        assertValidNotifyingOrg("MILITARY_COURT", "CATTERICK_MILITARY_COURT_CENTRE")
      }

      @ParameterizedTest(name = "it should return a validation error for name={0} when notifying org is PRISON")
      @ValueSource(strings = ["", "YORK_CROWN_COURT", "BELMARSH_MAGISTRATES_COURT"])
      fun `it should return a validation error for invalid names when notifying organisation is PRISON`(value: String) {
        assertInvalidNotifyingOrg("PRISON", value)
      }

      @Test
      fun `it should accept a prison name when notifying organisation is PRISON`() {
        assertValidNotifyingOrg("PRISON", "BELMARSH_PRISON")
      }

      @ParameterizedTest(name = "it should return a validation error for name={0} when notifying org is PROBATION")
      @ValueSource(strings = ["", "BELMARSH_PRISON", "BELMARSH_MAGISTRATES_COURT"])
      fun `it should return a validation error for invalid names when notifying organisation is PROBATION`(
        value: String,
      ) {
        assertInvalidNotifyingOrg("PROBATION", value)
      }

      @Test
      fun `it should accept a family court name when notifying organisation is PROBATION`() {
        assertValidNotifyingOrg("PROBATION", "WALES")
      }

      @ParameterizedTest(name = "it should return an error for name={0} when notifying org is YOUTH_COURT")
      @ValueSource(strings = ["", "BELMARSH_PRISON", "CARDIFF_CROWN_COURT"])
      fun `it should return a validation error for invalid names when notifying organisation is YOUTH_COURT`(
        value: String,
      ) {
        assertInvalidNotifyingOrg("YOUTH_COURT", value)
      }

      @Test
      fun `it should accept a youth court name when notifying organisation is YOUTH_COURT`() {
        assertValidNotifyingOrg("YOUTH_COURT", "WITHAM_YOUTH_COURT")
      }

      @ParameterizedTest(name = "it should return an error for name={0} when notifying org is YOUTH_CUSTODY_SERVICE")
      @ValueSource(strings = ["", "BELMARSH_PRISON", "CARDIFF_CROWN_COURT"])
      fun `it should return a validation error for invalid names when notifying organisation is YOUTH_CUSTODY_SERVICE`(
        value: String,
      ) {
        assertInvalidNotifyingOrg("YOUTH_CUSTODY_SERVICE", value)
      }

      @Test
      fun `it should accept a youth custody service name when notifying organisation is YOUTH_CUSTODY_SERVICE`() {
        assertValidNotifyingOrg("YOUTH_CUSTODY_SERVICE", "LONDON")
      }
    }

    private fun assertInvalidNotifyingOrg(notifyingOrg: String, notifyingOrgName: String) {
      val order = createStoredOrder(
        dataDictionaryVersion = DataDictionaryVersion.DDV5,
      )

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            buildMockRequest(
              notifyingOrganisation = notifyingOrg,
              notifyingOrganisationName = notifyingOrgName,
            ),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ValidationError::class.java)
        .returnResult()
        .responseBody

      assertThat(result).isNotNull
      assertThat(result?.sortedBy { it.field }).isEqualTo(
        listOf(
          ValidationError(
            "notifyingOrganisationName",
            ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_NAME_REQUIRED,
          ),
        ),
      )
    }

    private fun assertValidNotifyingOrg(notifyingOrg: String, notifyingOrgName: String) {
      val order = createStoredOrder(
        dataDictionaryVersion = DataDictionaryVersion.DDV5,
      )

      val interestedParties = webTestClient.put()
        .uri("/api/orders/${order.id}/interested-parties")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            buildMockRequest(
              notifyingOrganisation = notifyingOrg,
              notifyingOrganisationName = notifyingOrgName,
            ),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(InterestedParties::class.java)
        .returnResult()
        .responseBody!!

      assertThat(interestedParties.notifyingOrganisation).isEqualTo(notifyingOrg)
      assertThat(interestedParties.notifyingOrganisationName).isEqualTo(notifyingOrgName)
    }
  }
}
