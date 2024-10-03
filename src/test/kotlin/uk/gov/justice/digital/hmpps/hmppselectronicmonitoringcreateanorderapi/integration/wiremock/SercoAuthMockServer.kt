package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class SercoAuthMockServerExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
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
}
