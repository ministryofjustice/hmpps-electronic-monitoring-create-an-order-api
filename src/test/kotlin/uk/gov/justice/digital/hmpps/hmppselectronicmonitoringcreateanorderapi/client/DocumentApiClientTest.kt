package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import io.netty.handler.timeout.ReadTimeoutException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension.Companion.documentApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentUploadResponse
import java.util.*

@ActiveProfiles("test")
class DocumentApiClientTest : IntegrationTestBase() {

  @Autowired
  lateinit var documentApiClient: DocumentApiClient

  @Nested
  inner class GetDocument {
    @Nested
    @DisplayName("GET /documents/documentId/file")
    inner class GetDocument {
      @Test
      fun `It calls get file endpoint`() {
        val documentId = UUID.randomUUID()
        documentApi.stubGetDocument(documentId.toString())

        documentApiClient.getDocument(documentId.toString())

        documentApi.verify(1, getRequestedFor(urlMatching("/documents/$documentId/file")))
      }

      @Test
      fun `it should retry if server returned error first time`() {
        val documentId = UUID.randomUUID()
        documentApi.stubGetDocumentRetryScenario(documentId.toString())

        documentApiClient.getDocument(documentId.toString())

        documentApi.verify(2, getRequestedFor(urlMatching("/documents/$documentId/file")))
      }

      @Test
      fun `it should time out after 1 second`() {
        val documentId = UUID.randomUUID()
        documentApi.stubGetDocument(documentId.toString(), 1500)

        val exception = assertThrows<Exception> { documentApiClient.getDocument(documentId.toString()) }

        documentApi.verify(2, getRequestedFor(urlMatching("/documents/$documentId/file")))
        assertThat(
          exception.message,
        ).isEqualTo("Retries exhausted: 1/1")

        assertEquals(exception.cause?.cause?.javaClass, ReadTimeoutException::class.java)
      }
    }

    @Nested
    @DisplayName("Post /documents/CEMO_ATTACHMENT/documentUuid")
    inner class UploadDocument {

      private val documentUuid = UUID.randomUUID().toString()
      private val bodyBuilder = createMultiPartBodyBuilder(mockFile("filename1.txt"))

      @Test
      fun `It calls post file endpoint`() {
        val documentId = UUID.randomUUID()
        documentApi.stubUploadDocument(DocumentUploadResponse())

        documentApiClient.createDocument(documentUuid, bodyBuilder)

        documentApi.verify(1, postRequestedFor(urlMatching("/documents/CEMO_ATTACHMENT/$documentUuid")))
      }

      @Test
      fun `it should retry if server returned error first time`() {
        val documentId = UUID.randomUUID()
        documentApi.stubUploadDocumentRetryScenario(DocumentUploadResponse())

        documentApiClient.createDocument(documentUuid, bodyBuilder)

        documentApi.verify(2, postRequestedFor(urlMatching("/documents/CEMO_ATTACHMENT/$documentUuid")))
      }
    }
  }
}
