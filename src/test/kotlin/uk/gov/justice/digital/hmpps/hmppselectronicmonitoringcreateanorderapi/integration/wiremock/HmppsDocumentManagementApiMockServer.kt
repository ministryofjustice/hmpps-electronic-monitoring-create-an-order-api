package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentUploadResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.nio.file.Files
import java.nio.file.Paths

class HmppsDocumentManagementApiExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
  companion object {
    @JvmField
    val documentApi = HmppsDocumentManagementApi()
  }

  override fun beforeAll(context: ExtensionContext) {
    documentApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    documentApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    documentApi.stop()
  }
}

class HmppsDocumentManagementApi : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8092
  }

  private val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

  @Suppress("ktlint:standard:max-line-length")
  private val filePath: String = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppselectronicmonitoringcreateanorderapi/integration/assets/profile.jpeg"

  fun stubUploadDocument(result: DocumentUploadResponse?) {
    stubFor(
      post(urlPathTemplate("/documents/CEMO_ATTACHMENT/{uuid}"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              mapper.writeValueAsString(result),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubUploadDocumentBadRequest(error: ErrorResponse?) {
    stubFor(
      post(urlPathTemplate("/documents/CEMO_ATTACHMENT/{uuid}"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              mapper.writeValueAsString(error),
            )
            .withStatus(400),
        ),
    )
  }

  fun stubGetDocument(uuid: String = "xxx") {
    stubFor(
      get("/documents/$uuid/file")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "image/jpeg")
            .withBody(
              Files.readAllBytes(
                Paths.get(
                  this.filePath,
                ),
              ),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubDeleteDocument(uuid: String = "xxx") {
    stubFor(
      delete("/documents/$uuid")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200),
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
