
package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

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
}
