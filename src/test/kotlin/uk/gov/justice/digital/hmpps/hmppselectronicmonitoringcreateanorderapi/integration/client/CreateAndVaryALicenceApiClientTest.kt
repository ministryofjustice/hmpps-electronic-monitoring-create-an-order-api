package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.client

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import tools.jackson.module.kotlin.jacksonObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.CreateAndVaryALicenceApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.CreateAndVaryALicenceApiExtension.Companion.createAndVaryALicenceApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence.LicenceSummary
import java.time.LocalDateTime

@ActiveProfiles("test")
class CreateAndVaryALicenceApiClientTest : IntegrationTestBase() {

  @Autowired
  lateinit var createAndVaryALicenceApiClient: CreateAndVaryALicenceApiClient

  val licences = listOf(LicenceSummary("999999", "APPROVED", LocalDateTime.of(2020, 1, 1, 1, 1)))

  private val licenceConditions =
    this::class.java.getResource("/fixtures/cvl/licenceConditionsWithEM.json")!!.readText()

  @Nested
  @DisplayName("GET licence details")
  inner class GetLicenceDetails {
    @Test
    fun `it calls the get by crn endpoint`() {
      createAndVaryALicenceApi.stubGetLicenceByCrn(
        crn = "X123456",
        jacksonObjectMapper().writeValueAsString(licences),
      )

      createAndVaryALicenceApiClient.getLicenceByCrn("X123456")

      createAndVaryALicenceApi.verify(1, getRequestedFor(urlPathEqualTo("/public/licence-summaries/crn/X123456")))
    }

    @Test
    fun `it calls the get by crn endpoint and returns the latest approved licence`() {
      val licencesList = listOf(
        LicenceSummary("999997", "APPROVED", LocalDateTime.of(2025, 1, 1, 1, 1)),
        LicenceSummary("999998", "IN_PROGRESS", LocalDateTime.of(2027, 1, 1, 1, 1)),
        LicenceSummary("999999", "APPROVED", LocalDateTime.of(2026, 1, 1, 1, 1)),
      )

      createAndVaryALicenceApi.stubGetLicenceByCrn(
        crn = "X123456",
        jacksonObjectMapper().writeValueAsString(licencesList),
      )

      val result: LicenceSummary? = createAndVaryALicenceApiClient.getLicenceByCrn("X123456")

      createAndVaryALicenceApi.verify(1, getRequestedFor(urlPathEqualTo("/public/licence-summaries/crn/X123456")))
      assertThat(result?.id).isEqualTo("999999")
    }

    @Test
    fun `it calls the get by prisonNumber endpoint`() {
      createAndVaryALicenceApi.stubGetLicenceByPrisonNumber(
        prisonNumber = "X123456",
        jacksonObjectMapper().writeValueAsString(licences),
      )

      createAndVaryALicenceApiClient.getLicenceByPrisonNumber("X123456")

      createAndVaryALicenceApi.verify(
        1,
        getRequestedFor(urlPathEqualTo("/public/licence-summaries/prison-number/X123456")),
      )
    }

    @Test
    fun `it calls the get by prisonNumber endpoint and returns the latest approved licence`() {
      val licencesList = listOf(
        LicenceSummary("999997", "APPROVED", LocalDateTime.of(2025, 1, 1, 1, 1)),
        LicenceSummary("999998", "IN_PROGRESS", LocalDateTime.of(2027, 1, 1, 1, 1)),
        LicenceSummary("999999", "APPROVED", LocalDateTime.of(2026, 1, 1, 1, 1)),
      )

      createAndVaryALicenceApi.stubGetLicenceByPrisonNumber(
        prisonNumber = "X123456",
        jacksonObjectMapper().writeValueAsString(licencesList),
      )

      val result: LicenceSummary? = createAndVaryALicenceApiClient.getLicenceByPrisonNumber("X123456")

      createAndVaryALicenceApi.verify(
        1,
        getRequestedFor(urlPathEqualTo("/public/licence-summaries/prison-number/X123456")),
      )
      assertThat(result?.id).isEqualTo("999999")
    }

    @Test
    fun `it calls the get licence conditions endpoint`() {
      val licenceId = "1"
      createAndVaryALicenceApi.stubGetLicenceConditions(
        licenceId = licenceId,
        licenceConditions,
      )

      val result = createAndVaryALicenceApiClient.getLicenceConditions(licenceId)

      createAndVaryALicenceApi.verify(1, getRequestedFor(urlPathEqualTo("/public/v2/licences/id/$licenceId")))
      assertNotNull(result)
    }

    @Test
    fun `it calls the get licence conditions endpoint but no licence exists`() {
      val licenceId = "1"
      createAndVaryALicenceApi.stubGetLicenceConditions(
        licenceId = licenceId,
        "",
      )

      val result = createAndVaryALicenceApiClient.getLicenceConditions(licenceId)

      createAndVaryALicenceApi.verify(1, getRequestedFor(urlPathEqualTo("/public/v2/licences/id/$licenceId")))
      assertNull(result)
    }
  }
}
