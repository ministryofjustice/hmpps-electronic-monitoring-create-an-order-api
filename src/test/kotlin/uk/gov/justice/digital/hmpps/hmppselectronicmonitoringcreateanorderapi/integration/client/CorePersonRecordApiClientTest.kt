package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.client

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.CorePersonRecordApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordAuthorisationException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordDependencyException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordNotFoundException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.CorePersonRecordApiExtension.Companion.corePersonRecordApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.util.UUID

@ActiveProfiles("test")
class CorePersonRecordApiClientTest : IntegrationTestBase() {

  @Autowired
  lateinit var corePersonRecordApiClient: CorePersonRecordApiClient

  private val prisonerDetailsJson =
    this::class.java.getResource("/fixtures/corePersonRecord/prisonerDetails.json")!!.readText()

  @Nested
  @DisplayName("GET person details")
  inner class GetPersonDetails {
    @Test
    fun `it calls the get by prison number endpoint`() {
      corePersonRecordApi.stubGetPrisonerDetails("A1234BC", prisonerDetailsJson)

      corePersonRecordApiClient.getPersonByPrisonNumber("A1234BC", UUID.randomUUID())

      corePersonRecordApi.verify(1, getRequestedFor(urlPathEqualTo("/person/prison/A1234BC")))
    }

    @Test
    fun `it calls the get by probation crn endpoint`() {
      corePersonRecordApi.stubGetProbationDetails("X12345", prisonerDetailsJson)

      corePersonRecordApiClient.getPersonByCrn("X12345", UUID.randomUUID())

      corePersonRecordApi.verify(1, getRequestedFor(urlPathEqualTo("/person/probation/X12345")))
    }

    @Test
    fun `it deserialises and maps the response to a device wearer`() {
      val versionId = UUID.randomUUID()
      corePersonRecordApi.stubGetPrisonerDetails("A1234BC", prisonerDetailsJson)

      val result = corePersonRecordApiClient.getPersonByPrisonNumber("A1234BC", versionId)

      val deviceWearer = result.deviceWearer
      assertThat(deviceWearer?.versionId).isEqualTo(versionId)
      assertThat(deviceWearer?.firstName).isEqualTo("Bob")
      assertThat(deviceWearer?.lastName).isEqualTo("Builder")
      assertThat(deviceWearer?.nomisId).isEqualTo("A1234BC")
      assertThat(deviceWearer?.courtCaseReferenceNumber).isEqualTo("B123435")
      assertThat(deviceWearer?.pncId).isEqualTo("2000/1234567A")
      assertThat(deviceWearer?.nationalInsuranceNumber).isEqualTo("QQ123456B")
      assertThat(deviceWearer?.sex).isEqualTo("MALE")
      assertThat(deviceWearer?.alias).isEqualTo("Wendy Scoop Dizzy")
      assertThat(deviceWearer?.religion).isEqualTo("Christianity")
      assertThat(deviceWearer?.nationality).isEqualTo("GB")
    }

    @Test
    fun `it deserialises and maps the primary address and contact details`() {
      val versionId = UUID.randomUUID()
      corePersonRecordApi.stubGetPrisonerDetails("A1234BC", prisonerDetailsJson)

      val result = corePersonRecordApiClient.getPersonByPrisonNumber("A1234BC", versionId)

      val primaryAddress = result.addresses.firstOrNull { it.addressType == AddressType.PRIMARY }
      assertThat(primaryAddress?.addressLine1).isEqualTo("10 Downing Street")
      assertThat(primaryAddress?.postcode).isEqualTo("AB1 2CD")
      assertThat(result.contactDetails?.contactNumber).isEqualTo("07700900000")
    }

    @Test
    fun `it translates a not found response into a not found exception`() {
      corePersonRecordApi.stubGetPrisonerDetailsNotFound("A1234BC")

      assertThatThrownBy {
        corePersonRecordApiClient.getPersonByPrisonNumber("A1234BC", UUID.randomUUID())
      }.isInstanceOf(CorePersonRecordNotFoundException::class.java)
    }

    @Test
    fun `it translates a forbidden response into an authorisation exception`() {
      corePersonRecordApi.stubGetPrisonerDetailsForbidden("A1234BC")

      assertThatThrownBy {
        corePersonRecordApiClient.getPersonByPrisonNumber("A1234BC", UUID.randomUUID())
      }.isInstanceOf(CorePersonRecordAuthorisationException::class.java)
    }

    @Test
    fun `it translates a server error response into a dependency exception with the upstream status`() {
      corePersonRecordApi.stubGetPrisonerDetailsServerError("A1234BC")

      assertThatThrownBy {
        corePersonRecordApiClient.getPersonByPrisonNumber("A1234BC", UUID.randomUUID())
      }.isInstanceOf(CorePersonRecordDependencyException::class.java)
        .matches {
          (it as CorePersonRecordDependencyException).upstreamStatusCode == HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    @Test
    fun `it translates a connection failure into a dependency exception with no upstream status`() {
      corePersonRecordApi.stubGetPrisonerDetailsConnectionReset("A1234BC")

      assertThatThrownBy {
        corePersonRecordApiClient.getPersonByPrisonNumber("A1234BC", UUID.randomUUID())
      }.isInstanceOf(CorePersonRecordDependencyException::class.java)
        .matches {
          (it as CorePersonRecordDependencyException).upstreamStatusCode == null
        }
    }
  }
}
