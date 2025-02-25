package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.time.LocalDateTime
import java.time.ZoneOffset

class SercoAuthMockServerExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val sercoAuthApi = SercoAuthMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    sercoAuthApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    sercoAuthApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    sercoAuthApi.stop()
  }
}

class SercoAuthMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8093
  }
  fun stubError() {
    stubFor(
      post(urlEqualTo("/"))
        .willReturn(
          aResponse()
            .withStatus(403),
        ),
    )
  }
  fun stubGrantToken() {
    stubFor(
      post(urlEqualTo("/"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                {
                  "token_type": "bearer",
                  "access_token": "ABCDE",
                  "expires_in": ${LocalDateTime.now().plusHours(2).toEpochSecond(ZoneOffset.UTC)}
                }
              """.trimIndent(),
            ),
        ),
    )
  }
}
