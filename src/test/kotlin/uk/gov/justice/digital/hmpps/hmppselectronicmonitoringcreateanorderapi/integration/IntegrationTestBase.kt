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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.util.*

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
  lateinit var repo: OrderRepository

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  protected val testUser = "AUTH_ADM"

  internal fun setAuthorisation(
    username: String? = testUser,
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

  fun mockFile(fileName: String? = "file-name.jpeg", sizeInMB: Int? = 1): MockMultipartFile {
    val testFileContent: ByteArray = if (sizeInMB != null) {
      ByteArray(sizeInMB * 1024 * 1024) { 0 }
    } else {
      "Test file content".toByteArray()
    }

    return MockMultipartFile(
      "file",
      fileName,
      MediaType.IMAGE_JPEG_VALUE,
      testFileContent,
    )
  }

  fun createMultiPartBodyBuilder(multiPartFile: MockMultipartFile): MultipartBodyBuilder {
    val builder = MultipartBodyBuilder()

    builder.part("file", ByteArrayResource(multiPartFile.bytes))
      .header("Content-Disposition", "form-data; name=file; filename=${multiPartFile.originalFilename}")

    return builder
  }

  fun createOrder(username: String? = testUser): OrderDto = webTestClient.post()
    .uri("/api/orders")
    .headers(setAuthorisation(username))
    .exchange()
    .expectStatus()
    .isOk
    .returnResult(OrderDto::class.java)
    .responseBody.blockFirst()!!

  fun createVariation(username: String? = testUser): OrderDto = webTestClient.post()
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
    .returnResult(OrderDto::class.java)
    .responseBody.blockFirst()!!

  fun createStoredOrder(
    orderId: UUID = UUID.randomUUID(),
    username: String = testUser,
    status: OrderStatus = OrderStatus.IN_PROGRESS,
    type: RequestType = RequestType.REQUEST,
    dataDictionaryVersion: DataDictionaryVersion = DataDictionaryVersion.DDV4,
  ): Order {
    val order = Order(
      id = orderId,
      versions = mutableListOf(
        OrderVersion(
          orderId = orderId,
          status = status,
          type = type,
          username = username,
          dataDictionaryVersion = dataDictionaryVersion,
        ),
      ),
    )
    return repo.save(order)
  }
  fun createSubmittedOrder(
    type: RequestType = RequestType.REQUEST,
    dataDictionaryVersion: DataDictionaryVersion = DataDictionaryVersion.DDV4,
  ): Order = createStoredOrder(status = OrderStatus.SUBMITTED, dataDictionaryVersion = dataDictionaryVersion)

  fun createSubmittedVariation() = createSubmittedOrder(RequestType.VARIATION)

  fun getOrder(id: UUID) = webTestClient.get()
    .uri("/api/orders/$id")
    .headers(setAuthorisation(testUser))
    .exchange()
    .expectStatus()
    .isOk
    .expectBody(OrderDto::class.java)
    .returnResult()
    .responseBody!!
}
