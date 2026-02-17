package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserGroup
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsUserCaseloadResponse

class ManageUserApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val manageUserApi = ManageUserMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    manageUserApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    manageUserApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    manageUserApi.stop()
  }
}

class ManageUserMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8095
  }

  private val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

  fun stubUserActiveCaseLoad(caseLoad: HmppsUserCaseloadResponse) {
    stubFor(
      get(urlPathTemplate("/users/me/caseloads"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              mapper.writeValueAsString(caseLoad),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetUserGroups(groups: List<UserGroup>) {
    stubFor(
      get(urlPathTemplate("/users/me/groups"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              mapper.writeValueAsString(groups),
            )
            .withStatus(200),
        ),
    )
  }
}
