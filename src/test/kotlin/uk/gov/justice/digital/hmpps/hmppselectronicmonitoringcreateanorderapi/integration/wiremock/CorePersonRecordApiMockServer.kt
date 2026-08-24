package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class CorePersonRecordApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val corePersonRecordApi = CorePersonRecordApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    corePersonRecordApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    corePersonRecordApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    corePersonRecordApi.stop()
  }
}

class CorePersonRecordApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8096
  }

  fun stubGetPrisonerDetails(prisonNumber: String, responseBody: String) {
    stubFor(
      get(urlPathTemplate("/person/prison/{prisonNumber}"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
            .withStatus(200),
        ),
    )
  }

  fun stubGetPrisonerDetailsNotFound(prisonNumber: String) {
    stubFor(
      get(urlPathTemplate("/person/prison/{prisonNumber}"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(404),
        ),
    )
  }

  fun stubHealthPing(status: Int) {
    stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) """{"status":"UP"}""" else """{"status":"DOWN"}""")
          .withStatus(status),
      ),
    )
  }
}
