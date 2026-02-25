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
  private val mockResponsibleOrganisationEmail = "mockResponsibleOrganisationEmail"
  private val mockResponsibleOfficerFirstName = "mockResponsibleOfficerFirstName"
  private val mockResponsibleOfficerLastName = "mockResponsibleOfficerLastName"
  private val mockResponsibleOfficerEmail = "mockResponsibleOfficerEmail"

  private fun buildMockRequest(
    notifyingOrganisation: String? = mockNotifyingOrganisation,
    notifyingOrganisationName: String? = mockNotifyingOrganisationName,
    notifyingOrganisationEmail: String? = mockNotifyingOrganisationEmail,
    responsibleOfficerName: String? = mockResponsibleOfficerName,
    responsibleOfficerPhoneNumber: String? = mockResponsibleOfficerPhoneNumber,
    responsibleOrganisation: String? = mockResponsibleOrganisation,
    responsibleOrganisationRegion: String? = mockResponsibleOrganisationRegion,
    responsibleOrganisationEmail: String? = mockResponsibleOrganisationEmail,
    responsibleOfficerFirstName: String? = mockResponsibleOfficerFirstName,
    responsibleOfficerLastName: String? = mockResponsibleOfficerLastName,
    responsibleOfficerEmail: String? = mockResponsibleOfficerEmail,
  ): String = """
    {
      "notifyingOrganisation": "$notifyingOrganisation",
      "notifyingOrganisationName": "$notifyingOrganisationName",
      "notifyingOrganisationEmail": "$notifyingOrganisationEmail",
      "responsibleOfficerName": "$responsibleOfficerName",
      "responsibleOfficerPhoneNumber": "$responsibleOfficerPhoneNumber",
      "responsibleOrganisation": "$responsibleOrganisation",
      "responsibleOrganisationRegion": "$responsibleOrganisationRegion",
      "responsibleOrganisationEmail": "$responsibleOrganisationEmail",
      "responsibleOfficerFirstName": "$responsibleOfficerFirstName",
      "responsibleOfficerLastName": "$responsibleOfficerLastName",
      "responsibleOfficerEmail": "$responsibleOfficerEmail"
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
      Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo(mockResponsibleOrganisationEmail)
      Assertions.assertThat(interestedParties.responsibleOfficerFirstName).isEqualTo(mockResponsibleOfficerFirstName)
      Assertions.assertThat(interestedParties.responsibleOfficerLastName).isEqualTo(mockResponsibleOfficerLastName)
      Assertions.assertThat(interestedParties.responsibleOfficerEmail).isEqualTo(mockResponsibleOfficerEmail)
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

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo(mockNotifyingOrganisation)
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo("")
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
      Assertions.assertThat(
        interestedParties.responsibleOfficerPhoneNumber,
      ).isEqualTo(mockResponsibleOfficerPhoneNumber)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo(mockResponsibleOrganisation)
      Assertions.assertThat(
        interestedParties.responsibleOrganisationRegion,
      ).isEqualTo(mockResponsibleOrganisationRegion)
      Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo("")
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
      Assertions.assertThat(result?.sortedBy { it.field }).isEqualTo(
        listOf(
          ValidationError(
            "notifyingOrganisation",
            ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_REQUIRED,
          ),
          ValidationError(
            "notifyingOrganisationEmail",
            ValidationErrors.InterestedParties.TEAM_EMAIL_REQUIRED,
          ),

        ),
      )
    }

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

      Assertions.assertThat(result).isNotNull
      Assertions.assertThat(result?.sortedBy { it.field }).isEqualTo(
        listOf(
          ValidationError(
            "responsibleOrganisationRegion",
            ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_REGION_REQUIRED,
          ),
        ),
      )
    }

    @Test
    fun `it should accept empty name when notifying organisation is PROBATION`() {
      val order = createOrder()
      val interestedParties =
        webTestClient.put().uri("/api/orders/${order.id}/interested-parties").contentType(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(
              buildMockRequest(
                notifyingOrganisation = "PROBATION",
                notifyingOrganisationName = "",
              ),
            ),
          ).headers(setAuthorisation("AUTH_ADM")).exchange()
          .expectStatus().isOk.expectBody(InterestedParties::class.java).returnResult().responseBody!!

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo("PROBATION")
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo("")
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

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo(mockNotifyingOrganisation)
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo(mockNotifyingOrganisationName)
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
      Assertions.assertThat(
        interestedParties.responsibleOfficerPhoneNumber,
      ).isEqualTo(mockResponsibleOfficerPhoneNumber)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo("PROBATION")
      Assertions.assertThat(interestedParties.responsibleOrganisationRegion).isEqualTo("YORKSHIRE_AND_THE_HUMBER")
      Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo(mockResponsibleOrganisationEmail)
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
      Assertions.assertThat(result?.sortedBy { it.field }).isEqualTo(
        listOf(
          ValidationError(
            "notifyingOrganisationEmail",
            ValidationErrors.InterestedParties.TEAM_EMAIL_REQUIRED,
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

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo("PROBATION")
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo("")
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo("test@test.com")
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo("Jeff Testberg")
      Assertions.assertThat(interestedParties.responsibleOfficerPhoneNumber).isEqualTo("01234567890")
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo("YJS")
      Assertions.assertThat(interestedParties.responsibleOrganisationRegion).isEqualTo("NORTH_EAST_AND_CUMBRIA")
      Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo("")
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
            buildMockRequest(
              notifyingOrganisation = "PRISON",
              notifyingOrganisationName = value,
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

      Assertions.assertThat(result).isNotNull
      Assertions.assertThat(result?.sortedBy { it.field }).isEqualTo(
        listOf(
          ValidationError(
            "notifyingOrganisationName",
            ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_NAME_REQUIRED,
          ),
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
            buildMockRequest(
              notifyingOrganisation = "PRISON",
              notifyingOrganisationName = "BELMARSH_PRISON",
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

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo("PRISON")
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo("BELMARSH_PRISON")
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
      Assertions.assertThat(
        interestedParties.responsibleOfficerPhoneNumber,
      ).isEqualTo(mockResponsibleOfficerPhoneNumber)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo(mockResponsibleOrganisation)
      Assertions.assertThat(
        interestedParties.responsibleOrganisationRegion,
      ).isEqualTo(mockResponsibleOrganisationRegion)
      Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo(mockResponsibleOrganisationEmail)
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
            buildMockRequest(
              notifyingOrganisation = "CROWN_COURT",
              notifyingOrganisationName = value,
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

      Assertions.assertThat(result).isNotNull
      Assertions.assertThat(result?.sortedBy { it.field }).isEqualTo(
        listOf(
          ValidationError(
            "notifyingOrganisationName",
            ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_NAME_REQUIRED,
          ),
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
            buildMockRequest(
              notifyingOrganisation = "CROWN_COURT",
              notifyingOrganisationName = "YORK_CROWN_COURT",
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

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo("CROWN_COURT")
      Assertions.assertThat(interestedParties.notifyingOrganisationName).isEqualTo("YORK_CROWN_COURT")
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
      Assertions.assertThat(
        interestedParties.responsibleOfficerPhoneNumber,
      ).isEqualTo(mockResponsibleOfficerPhoneNumber)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo(mockResponsibleOrganisation)
      Assertions.assertThat(
        interestedParties.responsibleOrganisationRegion,
      ).isEqualTo(mockResponsibleOrganisationRegion)
      Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo(mockResponsibleOrganisationEmail)
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
            buildMockRequest(
              notifyingOrganisation = "MAGISTRATES_COURT",
              notifyingOrganisationName = value,
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

      Assertions.assertThat(result).isNotNull
      Assertions.assertThat(result?.sortedBy { it.field }).isEqualTo(
        listOf(
          ValidationError(
            "notifyingOrganisationName",
            ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_NAME_REQUIRED,
          ),
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
            buildMockRequest(
              notifyingOrganisation = "MAGISTRATES_COURT",
              notifyingOrganisationName = "BRADFORD_AND_KEIGHLEY_MAGISTRATES_COURT",
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

      Assertions.assertThat(interestedParties.notifyingOrganisation).isEqualTo("MAGISTRATES_COURT")
      Assertions.assertThat(
        interestedParties.notifyingOrganisationName,
      ).isEqualTo("BRADFORD_AND_KEIGHLEY_MAGISTRATES_COURT")
      Assertions.assertThat(interestedParties.notifyingOrganisationEmail).isEqualTo(mockNotifyingOrganisationEmail)
      Assertions.assertThat(interestedParties.responsibleOfficerName).isEqualTo(mockResponsibleOfficerName)
      Assertions.assertThat(
        interestedParties.responsibleOfficerPhoneNumber,
      ).isEqualTo(mockResponsibleOfficerPhoneNumber)
      Assertions.assertThat(interestedParties.responsibleOrganisation).isEqualTo(mockResponsibleOrganisation)
      Assertions.assertThat(
        interestedParties.responsibleOrganisationRegion,
      ).isEqualTo(mockResponsibleOrganisationRegion)
      Assertions.assertThat(interestedParties.responsibleOrganisationEmail).isEqualTo(mockResponsibleOrganisationEmail)
    }
  }
}
