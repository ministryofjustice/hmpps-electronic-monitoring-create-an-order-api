package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class CreateAndVaryALicenceApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val createAndVaryALicenceApi = CreateAndVaryALicenceApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    createAndVaryALicenceApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    createAndVaryALicenceApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    createAndVaryALicenceApi.stop()
  }
}

class CreateAndVaryALicenceApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8097
  }

  fun stubGetLicenceByCrn(crn: String, responseBody: String) {
    stubFor(
      get(urlPathTemplate("/public/licence-summaries/crn/$crn"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
            .withStatus(200),
        ),
    )
  }

  fun stubGetLicenceByPrisonNumber(prisonNumber: String, responseBody: String) {
    stubFor(
      get(urlPathTemplate("/public/licence-summaries/prison-number/$prisonNumber"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
            .withStatus(200),
        ),
    )
  }

  fun stubGetLicenceConditions(licenceId: String, responseBody: String) {
    stubFor(
      get(urlPathTemplate("/public/v2/licences/id/$licenceId"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
            .withStatus(200),
        ),
    )
  }
}
