
package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse

class SercoMockApiExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
  companion object {
    @JvmField
    val sercoApi = SercoMockApiServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    sercoApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    sercoApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    sercoApi.stop()
  }
}

class SercoMockApiServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8094
  }

  private val objectMapper: ObjectMapper = ObjectMapper()
  fun stupCreateDeviceWearer(jsonBody: String, status: HttpStatus, result: FmsResponse) {
    stubFor(
      post(urlPathTemplate("/device_wearer/createDW"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(result),
            )
            .withStatus(status.value()),
        ),
    )
  }

  fun stupMonitoringOrder(jsonBody: String, status: HttpStatus, result: FmsResponse) {
    stubFor(
      post(urlPathTemplate("/monitoring_order/createMO"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(result),
            )
            .withStatus(status.value()),
        ),
    )
  }
}
