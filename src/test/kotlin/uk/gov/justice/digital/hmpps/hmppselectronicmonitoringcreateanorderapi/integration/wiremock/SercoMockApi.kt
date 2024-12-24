
package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
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
  fun stubCreateDeviceWearer(status: HttpStatus, result: FmsResponse, errorResponse: FmsErrorResponse? = null) {
    val body: String
    if (errorResponse != null) {
      body = objectMapper.writeValueAsString(errorResponse)
    } else {
      body = objectMapper.writeValueAsString(result)
    }
    stubFor(
      post(urlPathTemplate("/device_wearer/createDW"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              body,
            )
            .withStatus(status.value()),
        ),
    )
  }

  fun stubCreateMonitoringOrder(status: HttpStatus, result: FmsResponse, errorResponse: FmsErrorResponse? = null) {
    val body: String
    if (errorResponse != null) {
      body = objectMapper.writeValueAsString(errorResponse)
    } else {
      body = objectMapper.writeValueAsString(result)
    }
    stubFor(
      post(urlPathTemplate("/monitoring_order/createMO"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              body,
            )
            .withStatus(status.value()),
        ),
    )
  }

  fun stubUpdateMonitoringOrder(status: HttpStatus, result: FmsResponse, errorResponse: FmsErrorResponse? = null) {
    val body: String
    if (errorResponse != null) {
      body = objectMapper.writeValueAsString(errorResponse)
    } else {
      body = objectMapper.writeValueAsString(result)
    }
    stubFor(
      post(urlPathTemplate("/monitoring_order/updateMO"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              body,
            )
            .withStatus(status.value()),
        ),
    )
  }

  fun stubSubmitAttachment(status: HttpStatus, result: FmsAttachmentResponse, errorResponse: FmsErrorResponse? = null) {
    val body: String

    if (errorResponse != null) {
      body = objectMapper.writeValueAsString(errorResponse)
    } else {
      body = objectMapper.writeValueAsString(result)
    }

    stubFor(
      post(urlPathTemplate("/attachment_csm/file"))
        .withQueryParam("table_name", equalTo(result.result.tableName))
        .withQueryParam("table_sys_id", equalTo(result.result.tableSysId))
        .withQueryParam("file_name", equalTo(result.result.fileName))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              body,
            )
            .withStatus(status.value()),
        ),
    )
  }
}
