package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import FmsStateResponse
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpStatus
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRetrieveDWandMO

class SercoMockApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
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

  private fun responseBody(result: Any, errorResponse: FmsErrorResponse?): String =
    objectMapper.writeValueAsString(errorResponse ?: result)

  private fun stubPostResponse(path: String, status: HttpStatus, body: String) {
    stubFor(
      post(urlPathTemplate(path))
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

  fun stubCreateDeviceWearer(status: HttpStatus, result: FmsResponse, errorResponse: FmsErrorResponse? = null) {
    stubPostResponse("/x_seem_cemo/device_wearer/createDW", status, responseBody(result, errorResponse))
  }

  fun stubUpdateDeviceWearer(status: HttpStatus, result: FmsResponse, errorResponse: FmsErrorResponse? = null) {
    stubPostResponse("/x_seem_cemo/device_wearer/updateDW", status, responseBody(result, errorResponse))
  }

  fun stubCreateMonitoringOrder(status: HttpStatus, result: FmsResponse, errorResponse: FmsErrorResponse? = null) {
    stubPostResponse("/x_seem_cemo/monitoring_order/createMO", status, responseBody(result, errorResponse))
  }

  fun stubUpdateMonitoringOrder(status: HttpStatus, result: FmsResponse, errorResponse: FmsErrorResponse? = null) {
    stubPostResponse("/x_seem_cemo/monitoring_order/updateMO", status, responseBody(result, errorResponse))
  }

  fun stubCreateCommonPlatformDeviceWearer(
    status: HttpStatus,
    result: FmsResponse,
    errorResponse: FmsErrorResponse? = null,
  ) {
    stubPostResponse("/x_seem_cemo/device_wearer/createCPDW", status, responseBody(result, errorResponse))
  }

  fun stubCreateCommonPlatformMonitoringOrder(
    status: HttpStatus,
    result: FmsResponse,
    errorResponse: FmsErrorResponse? = null,
  ) {
    stubPostResponse("/x_seem_cemo/monitoring_order/createCPMO", status, responseBody(result, errorResponse))
  }

  fun stubUpdateCommonPlatformDeviceWearer(
    status: HttpStatus,
    result: FmsResponse,
    errorResponse: FmsErrorResponse? = null,
  ) {
    stubPostResponse("/x_seem_cemo/device_wearer/updateCPDW", status, responseBody(result, errorResponse))
  }

  fun stubUpdateCommonPlatformMonitoringOrder(
    status: HttpStatus,
    result: FmsResponse,
    errorResponse: FmsErrorResponse? = null,
  ) {
    stubPostResponse("/x_seem_cemo/monitoring_order/updateCPMO", status, responseBody(result, errorResponse))
  }

  fun stubSubmitAttachment(status: HttpStatus, result: FmsAttachmentResponse, errorResponse: FmsErrorResponse? = null) {
    val body = responseBody(result, errorResponse)

    stubFor(
      post(urlPathTemplate("/now/v1/attachment_csm/file"))
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

  fun stubGetState(
    caseId: String,
    status: HttpStatus,
    result: FmsStateResponse,
    errorResponse: FmsErrorResponse? = null,
  ) {
    val body = responseBody(result, errorResponse)
    stubFor(
      get(urlEqualTo("/now/table/x_serg2_ems_csm_case/$caseId?sysparm_fields=state"))
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

  fun stubGetDWandMo(caseId: String, status: HttpStatus, result: FmsRetrieveDWandMO, errorResponse: String? = null) {
    val body = errorResponse ?: responseBody(result, null)
    stubFor(
      get(urlEqualTo("/monitoring_order/retrieveDWandMO?u_case_id=$caseId"))
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
