package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(
  HmppsAuthApiExtension::class,
  HmppsDocumentManagementApiExtension::class,
  SercoAuthMockServerExtension::class,
  SercoMockApiExtension::class,
)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf("ROLE_EM_CEMO__CREATE_ORDER"),
    scopes: List<String> = listOf("read"),
  ): (
    HttpHeaders,
  ) -> Unit = jwtAuthHelper.setAuthorisationHeader(
    username = username,
    scope = scopes,
    roles = roles,
  )

  internal fun setAuthorisationWithoutUsername(
    roles: List<String> = listOf("ROLE_EM_CEMO__CREATE_ORDER"),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(scope = scopes, roles = roles)
  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  fun mockFile(fileName: String? = "file-name.jpeg"): MockMultipartFile {
    return MockMultipartFile(
      "file",
      fileName,
      MediaType.IMAGE_JPEG_VALUE,
      "Test file content".toByteArray(),
    )
  }

  fun createMultiPartBodyBuilder(multiPartFile: MockMultipartFile): MultipartBodyBuilder {
    val builder = MultipartBodyBuilder()

    builder.part("file", ByteArrayResource(multiPartFile.bytes))
      .header("Content-Disposition", "form-data; name=file; filename=${multiPartFile.originalFilename}")

    return builder
  }

  fun createOrder(username: String? = "AUTH_ADM"): Order = webTestClient.post()
    .uri("/api/orders")
    .headers(setAuthorisation(username))
    .exchange()
    .expectStatus()
    .isOk
    .returnResult(Order::class.java)
    .responseBody.blockFirst()!!

  fun createVariation(username: String? = "AUTH_ADM"): Order = webTestClient.post()
    .uri("/api/orders")
    .contentType(MediaType.APPLICATION_JSON)
    .body(
      BodyInserters.fromValue(
        """
            {
              "type": "VARIATION"
            }
        """.trimIndent(),
      ),
    )
    .headers(setAuthorisation(username))
    .exchange()
    .expectStatus()
    .isOk
    .returnResult(Order::class.java)
    .responseBody.blockFirst()!!
}
