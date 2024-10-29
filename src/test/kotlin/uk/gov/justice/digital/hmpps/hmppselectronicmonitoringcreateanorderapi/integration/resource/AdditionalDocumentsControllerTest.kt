package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.internal.verification.Times
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.mock.web.MockMultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension.Companion.documentApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentUploadResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.AdditionalDocumentRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.nio.file.Files
import java.nio.file.Paths

class AdditionalDocumentsControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var repo: OrderRepository

  @SpyBean
  lateinit var documentRepo: AdditionalDocumentRepository

  @SpyBean
  lateinit var apiCLient: DocumentApiClient
  val order = Order(username = "mockUser", status = OrderStatus.IN_PROGRESS)

  @BeforeEach
  fun setup() {
    repo.deleteAll()
    documentRepo.deleteAll()
    repo.save(order)
  }

  fun mockFile(fileName: String? = "file-name.jpeg"): MockMultipartFile {
    return MockMultipartFile(
      "file",
      "file-name.jpeg",
      MediaType.IMAGE_JPEG_VALUE,
      "Test file content".toByteArray(),
    )
  }

  @Test
  fun `file extension not allow return bad request with validation error`() {
    val bodyBuilder = MultipartBodyBuilder()
    bodyBuilder.part("file", ByteArrayResource(mockFile("filename2.txt").bytes))
      .header("Content-Disposition", "form-data; name=file; filename=filename2.txt")

    val result = webTestClient.post()
      .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
      .bodyValue(bodyBuilder.build())
      .headers(setAuthorisation("mockUser"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()

    val error = result.responseBody!!.first()
    Assertions.assertThat(error.userMessage).isEqualTo("Validation failure: Unsupported or missing file type txt. Supported file types: pdf, png, jpeg, jpg")
  }

  @Test
  fun `Document management api validation failed, return document management api error`() {
    val bodyBuilder = MultipartBodyBuilder()
    bodyBuilder.part("file", ByteArrayResource(mockFile().bytes))
      .header("Content-Disposition", "form-data; name=file; filename=file-name.jpeg")
    documentApi.stupUploadDocumentBadRequest(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "mock document api error",
        developerMessage = "",
      ),
    )

    val result = webTestClient.post()
      .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
      .bodyValue(bodyBuilder.build())
      .headers(setAuthorisation("mockUser"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()

    val error = result.responseBody!!.first()
    Assertions.assertThat(error.userMessage)
      .isEqualTo("mock document api error")
  }

  @Test
  fun `remove existing document when upload new document with same type`() {
    val doc = AdditionalDocument(
      orderId = order.id,
      fileName = "mockFile1",
      fileType = DocumentType.PHOTO_ID,
    )
    order.additionalDocuments.add(doc)
    repo.save(order)
    val bodyBuilder = MultipartBodyBuilder()
    bodyBuilder.part("file", ByteArrayResource(mockFile().bytes))
      .header("Content-Disposition", "form-data; name=file; filename=filename2.jpeg")

    documentApi.stupDeleteDocument(doc.id.toString())
    webTestClient.post()
      .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
      .bodyValue(bodyBuilder.build())
      .headers(setAuthorisation("mockUser"))
      .exchange()

    verify(documentRepo, Times(1)).deleteById(doc.id)
    verify(apiCLient, Times(1)).deleteDocument(doc.id.toString())
  }

  @Test
  fun `Save new document to db and document management api`() {
    val bodyBuilder = MultipartBodyBuilder()
    bodyBuilder.part("file", ByteArrayResource(mockFile().bytes))
      .header("Content-Disposition", "form-data; name=file; filename=file-name.jpeg")
    documentApi.stupUploadDocument(DocumentUploadResponse())

    webTestClient.post()
      .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
      .bodyValue(bodyBuilder.build())
      .headers(setAuthorisation("mockUser"))
      .exchange()
      .expectStatus()
      .isOk

    argumentCaptor<AdditionalDocument>().apply {
      verify(documentRepo, times(1)).save(capture())
      Assertions.assertThat(firstValue.orderId).isEqualTo(order.id)
      Assertions.assertThat(firstValue.fileType).isEqualTo(DocumentType.PHOTO_ID)
      Assertions.assertThat(firstValue.fileName).isEqualTo("file-name.jpeg")
    }
    argumentCaptor<String, MultipartBodyBuilder>().apply {
      verify(apiCLient, times(1)).createDocument(first.capture(), second.capture())
      val multipartBody = second.firstValue.build()
      Assertions.assertThat(multipartBody["file"]?.get(0)).isNotNull
      Assertions.assertThat(multipartBody["metadata"]?.get(0)?.body.toString()).isEqualTo("DocumentMetadata(orderId=${order.id}, documentType=PHOTO_ID)")
    }
  }

  @Test
  fun `get raw document, document not found, return not found`() {
    webTestClient.get()
      .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}/raw")
      .headers(setAuthorisation("mockUser"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `get raw document, return document from document management api`() {
    val doc = AdditionalDocument(
      orderId = order.id,
      fileName = "mockFile1",
      fileType = DocumentType.PHOTO_ID,
    )
    order.additionalDocuments.add(doc)
    repo.save(order)

    documentApi.stupGetDocument(doc.id.toString())
    val expectedBytes = Files.readAllBytes(Paths.get("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppselectronicmonitoringcreateanorderapi/integration/assets/profile.jpeg"))
    var result = webTestClient.get()
      .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}/raw")
      .headers(setAuthorisation("mockUser"))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.IMAGE_JPEG)
      .expectBody()
      .returnResult()
      .responseBody?.let { actualBytes ->
        Assertions.assertThat(actualBytes).isEqualTo(expectedBytes)
      }
  }
}
