package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension.Companion.documentApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentUploadResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.nio.file.Files
import java.nio.file.Paths

class AdditionalDocumentsControllerTest : IntegrationTestBase() {

  @MockitoSpyBean
  lateinit var apiClient: DocumentApiClient

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Nested
  @DisplayName("POST /api/orders/{orderId}/document-type/{documentType}")
  inner class UploadDocument {

    @Nested
    @DisplayName("Photo ID upload")
    inner class PhotoIDUpload {

      @Test
      fun `it should return a validation error for an invalid file extension`() {
        val order = createOrder()
        val bodyBuilder = createMultiPartBodyBuilder(mockFile("filename1.txt"))

        val result = webTestClient.post()
          .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation("AUTH_ADM"))
          .exchange()
          .expectStatus()
          .isBadRequest
          .expectBodyList(ErrorResponse::class.java)
          .returnResult()

        Assertions.assertThat(result.responseBody!!).contains(
          ErrorResponse(
            status = BAD_REQUEST,
            developerMessage = "Unsupported or missing file type txt. Supported file types: pdf, png, jpeg, jpg",
            userMessage =
            "Validation failure: Unsupported or missing file type txt. Supported file types: pdf, png, jpeg, jpg",
          ),
        )
      }

      @Test
      fun `it should return a validation error for a file that is too large`() {
        val order = createOrder()
        val bodyBuilder = createMultiPartBodyBuilder(mockFile("filename2.pdf", 11))

        val result = webTestClient.post()
          .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation("AUTH_ADM"))
          .exchange()
          .expectStatus()
          .isBadRequest
          .expectBodyList(ErrorResponse::class.java)
          .returnResult()

        Assertions.assertThat(result.responseBody!!).contains(
          ErrorResponse(
            status = BAD_REQUEST,
            developerMessage = "Maximum upload size of 10MB exceeded",
            userMessage = "File uploaded is larger than the max file size limit of 10MB",
          ),
        )
      }
    }

    @Nested
    @DisplayName("Licence upload")
    inner class LicenceUpload {

      @Test
      fun `it should return a validation error for an invalid file extension`() {
        val order = createOrder()
        val bodyBuilder = createMultiPartBodyBuilder(mockFile("filename3.txt"))

        val result = webTestClient.post()
          .uri("/api/orders/${order.id}/document-type/${DocumentType.LICENCE}")
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation("AUTH_ADM"))
          .exchange()
          .expectStatus()
          .isBadRequest
          .expectBodyList(ErrorResponse::class.java)
          .returnResult()

        Assertions.assertThat(result.responseBody!!).contains(
          ErrorResponse(
            status = BAD_REQUEST,
            developerMessage = "Unsupported or missing file type txt. Supported file types: pdf, doc, docx",
            userMessage =
            "Validation failure: Unsupported or missing file type txt. Supported file types: pdf, doc, docx",
          ),
        )
      }

      @Test
      fun `it should return a validation error for a file that is too large`() {
        val order = createOrder()
        val bodyBuilder = createMultiPartBodyBuilder(mockFile("filename4.pdf", 26))

        val result = webTestClient.post()
          .uri("/api/orders/${order.id}/document-type/${DocumentType.LICENCE}")
          .bodyValue(bodyBuilder.build())
          .headers(setAuthorisation("AUTH_ADM"))
          .exchange()
          .expectStatus()
          .isBadRequest
          .expectBodyList(ErrorResponse::class.java)
          .returnResult()

        Assertions.assertThat(result.responseBody!!).contains(
          ErrorResponse(
            status = BAD_REQUEST,
            developerMessage = "Maximum upload size of 25MB exceeded",
            userMessage = "File uploaded is larger than the max file size limit of 25MB",
          ),
        )
      }
    }

    @Test
    fun `it should return an error if upload to document management api fails`() {
      val order = createOrder()
      val bodyBuilder = createMultiPartBodyBuilder(mockFile())

      // Return an error from the document api
      documentApi.stubUploadDocumentBadRequest(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "mock document api error",
          developerMessage = "",
        ),
      )

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = BAD_REQUEST,
          developerMessage = "",
          userMessage = "mock document api error",
        ),
      )
    }

    @Test
    fun `it should upload a document to the document api`() {
      val order = createOrder()
      val bodyBuilder = createMultiPartBodyBuilder(mockFile())

      documentApi.stubUploadDocument(DocumentUploadResponse())

      // Upload document
      webTestClient.post()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      // Get updated order
      val updatedOrder = getOrder(order.id)

      // Verify order state
      Assertions.assertThat(updatedOrder.additionalDocuments).hasSize(1)
      Assertions.assertThat(updatedOrder.additionalDocuments[0].fileName).isEqualTo("file-name.jpeg")
      Assertions.assertThat(updatedOrder.additionalDocuments[0].fileType).isEqualTo(DocumentType.PHOTO_ID)

      // Verify document api stub received request
      argumentCaptor<String, MultipartBodyBuilder>().apply {
        verify(apiClient, times(1)).createDocument(first.capture(), second.capture())
        val multipartBody = second.firstValue.build()
        Assertions.assertThat(multipartBody["file"]?.get(0)).isNotNull
        Assertions.assertThat(
          multipartBody["metadata"]?.get(0)?.body.toString(),
        ).isEqualTo("DocumentMetadata(orderId=${order.id}, documentType=PHOTO_ID)")
      }
    }

    @Test
    fun `it should replace a document with same file type`() {
      val order = createOrder()
      val bodyBuilder = createMultiPartBodyBuilder(mockFile())
      val bodyBuilder2 = createMultiPartBodyBuilder(mockFile("file-name-2.jpeg"))

      documentApi.stubUploadDocument(DocumentUploadResponse())
      documentApi.stubDeleteDocument("(.*)")

      // Upload the first document
      webTestClient.post()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      // Upload the second document
      webTestClient.post()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .bodyValue(bodyBuilder2.build())
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      // Get updated order
      val updatedOrder = getOrder(order.id)

      // Verify order state
      Assertions.assertThat(updatedOrder.additionalDocuments).hasSize(1)
      Assertions.assertThat(updatedOrder.additionalDocuments[0].fileName).isEqualTo("file-name-2.jpeg")
      Assertions.assertThat(updatedOrder.additionalDocuments[0].fileType).isEqualTo(DocumentType.PHOTO_ID)
    }

    @Test
    fun `it should return an error if the order is in a submitted state`() {
      val order = createSubmittedOrder()

      val bodyBuilder = createMultiPartBodyBuilder(mockFile())

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = NOT_FOUND,
          developerMessage = "An editable order with ${order.id} does not exist",
          userMessage = "Not Found",
        ),
      )
    }

    @Test
    fun `it should return an error if the order is not created by the user`() {
      val order = createOrder()
      val bodyBuilder = createMultiPartBodyBuilder(mockFile())

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation("AUTH_ADM_2"))
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = NOT_FOUND,
          developerMessage = "An editable order with ${order.id} does not exist",
          userMessage = "Not Found",
        ),
      )
    }
  }

  @Nested
  @DisplayName("GET /api/orders/{orderId}/document-type/{documentType}/raw")
  inner class GetDocument {
    @Suppress("ktlint:standard:max-line-length")
    private val filePath = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppselectronicmonitoringcreateanorderapi/integration/assets/profile.jpeg"

    @Test
    fun `it should return not found if the document does not exist`() {
      val order = createOrder()

      webTestClient.get()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}/raw")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `it should return an error if the order was not created by the user`() {
      val order = createOrder()

      val result = webTestClient.get()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}/raw")
        .headers(setAuthorisation("AUTH_ADM_2"))
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = NOT_FOUND,
          developerMessage = "An editable order with ${order.id} does not exist",
          userMessage = "Not Found",
        ),
      )
    }

    @Test
    fun `it should return the raw document from the document management api`() {
      val order = createOrder()
      val bodyBuilder = createMultiPartBodyBuilder(mockFile())

      // Stub the document api
      documentApi.stubUploadDocument(DocumentUploadResponse())
      documentApi.stubGetDocument("(.*)")

      // Upload a document
      webTestClient.post()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}/raw")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.IMAGE_JPEG)
        .expectBody()
        .returnResult()
        .responseBody?.let { actualBytes ->
          Assertions.assertThat(actualBytes).isEqualTo(
            Files.readAllBytes(
              Paths.get(
                this.filePath,
              ),
            ),
          )
        }
    }
  }

  @Nested
  @DisplayName("DELETE /api/orders/{orderId}/document-type/{documentType}")
  inner class DeleteDocument {
    @Test
    fun `it should return an error if the order is in a submitted state`() {
      val order = createSubmittedOrder()

      val result = webTestClient.delete()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .headers(setAuthorisation("mockUser"))
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = NOT_FOUND,
          developerMessage = "An editable order with ${order.id} does not exist",
          userMessage = "Not Found",
        ),
      )
    }

    @Test
    fun `it should return an error if the order is not created by the user`() {
      val order = createOrder()

      val result = webTestClient.delete()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .headers(setAuthorisation("AUTH_ADM_2"))
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      Assertions.assertThat(result.responseBody!!).contains(
        ErrorResponse(
          status = NOT_FOUND,
          developerMessage = "An editable order with ${order.id} does not exist",
          userMessage = "Not Found",
        ),
      )
    }

    @Test
    fun `it should remove document from database and from document management api`() {
      val order = createOrder()
      val bodyBuilder = createMultiPartBodyBuilder(mockFile())

      // Stub the document api
      documentApi.stubUploadDocument(DocumentUploadResponse())
      documentApi.stubDeleteDocument("(.*)")

      // Upload a document
      webTestClient.post()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .bodyValue(bodyBuilder.build())
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      // Delete document
      webTestClient.delete()
        .uri("/api/orders/${order.id}/document-type/${DocumentType.PHOTO_ID}")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNoContent

      // Get updated order
      val updatedOrder = getOrder(order.id)

      Assertions.assertThat(updatedOrder.additionalDocuments).hasSize(0)
    }
  }
}
