package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Flux
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.io.ByteArrayInputStream
import java.util.*

@ActiveProfiles("test")
@JsonTest
class AdditionalDocumentServiceTest {
  private lateinit var service: AdditionalDocumentService
  private lateinit var client: DocumentApiClient
  private lateinit var orderRepo: OrderRepository

  val username: String = "username"
  val mockOrderId = UUID.randomUUID()
  val mockVersionId = UUID.randomUUID()
  val mockDictionaryVersion = DataDictionaryVersion.DDV4

  val docType: DocumentType = DocumentType.LICENCE
  val doc: AdditionalDocument = AdditionalDocument(versionId = mockVersionId, fileType = docType)

  @BeforeEach
  fun setup() {
    orderRepo = mock(OrderRepository::class.java)
    client = mock(DocumentApiClient::class.java)
    service = AdditionalDocumentService(client)
    service.orderRepo = orderRepo
  }

  @Nested
  @DisplayName("getDocument")
  inner class GetDocument {
    @Test
    fun `it should throw an error if document does not exist`() {
      // Mock an order with no documents
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                username = username,
                orderId = mockOrderId,
                dataDictionaryVersion = mockDictionaryVersion,
              ),
            ),
          ),
        ),
      )

      // Verify that an error is thrown
      val e = assertThrows(EntityNotFoundException::class.java) {
        service.getDocument(mockOrderId, username, docType)
      }

      Assertions.assertThat(e.message).isEqualTo("Document for $mockOrderId with type $docType not found")
    }

    @Test
    fun `it should return the object blob if the document exists`() {
      // Mock an order with a single document
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                username = username,
                orderId = mockOrderId,
                additionalDocuments = mutableListOf(
                  doc,
                ),
                dataDictionaryVersion = mockDictionaryVersion,
              ),
            ),
          ),
        ),
      )

      // Mock the document api response
      `when`(client.getDocument(mockOrderId.toString())).thenReturn(
        ResponseEntity.ok().body(
          Flux.just(InputStreamResource(ByteArrayInputStream("".toByteArray()))),
        ),
      )

      // Get the document
      val result = service.getDocument(mockOrderId, username, docType)

      // Verify the document api was called
      verify(client, times(1)).getDocument(doc.id.toString())
    }
  }

  @Nested
  @DisplayName("deleteDocument")
  inner class DeleteDocument {
    @Test
    fun `it should delete the document from the database and the document management api`() {
      // Mock an order with a single document
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                username = username,
                orderId = mockOrderId,
                additionalDocuments = mutableListOf(
                  doc,
                ),
                dataDictionaryVersion = mockDictionaryVersion,
              ),
            ),
          ),
        ),
      )

      // Delete the document
      service.deleteDocument(mockOrderId, username, docType)

      // Verify that the document was removed from the order
      verify(orderRepo, times(1)).save(
        Order(
          id = mockOrderId,
          versions = mutableListOf(
            OrderVersion(
              id = mockVersionId,
              status = OrderStatus.IN_PROGRESS,
              type = RequestType.REQUEST,
              username = username,
              versionId = 0,
              orderId = mockOrderId,
              additionalDocuments = mutableListOf(),
              dataDictionaryVersion = mockDictionaryVersion,
            ),
          ),
        ),

      )

      // Verify that the file was deleted from document api
      argumentCaptor<String>().apply {
        verify(client, times(1)).deleteDocument(capture())
        Assertions.assertThat(firstValue).isEqualTo(doc.id.toString())
      }
    }
  }

  @Nested
  @DisplayName("uploadDocument")
  inner class UploadDocument {
    @Test
    fun `it should throw an error for unsupported document extensions`() {
      // Mock an order with no documents
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                username = username,
                orderId = mockOrderId,
                dataDictionaryVersion = mockDictionaryVersion,
              ),
            ),
          ),
        ),
      )

      val e = assertThrows(ValidationException::class.java) {
        service.uploadDocument(
          mockOrderId,
          username,
          docType,
          MockMultipartFile(
            "file",
            "file-name.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Test file content".toByteArray(),
          ),
        )
      }
      Assertions.assertThat(
        e.message,
      ).isEqualTo("Unsupported or missing file type txt. Supported file types: pdf, png, jpeg, jpg")
    }

    @Test
    fun `it should replace a document with same file type`() {
      // Mock a UUID for new document
      val defaultUuid = UUID.randomUUID()
      Mockito.mockStatic(UUID::class.java).use {
        it.`when`<Any> { UUID.randomUUID() }.thenReturn(defaultUuid)

        // Mock an order with a single pdf
        whenever(
          orderRepo.findById(mockOrderId),
        ).thenReturn(
          Optional.of(
            Order(
              id = mockOrderId,
              versions = mutableListOf(
                OrderVersion(
                  id = mockVersionId,
                  status = OrderStatus.IN_PROGRESS,
                  type = RequestType.REQUEST,
                  username = username,
                  orderId = mockOrderId,
                  additionalDocuments = mutableListOf(
                    doc,
                  ),
                  dataDictionaryVersion = mockDictionaryVersion,
                ),
              ),
            ),
          ),
        )

        // Upload a new pdf
        service.uploadDocument(
          mockOrderId,
          username,
          docType,
          MockMultipartFile(
            "file",
            "file-name.pdf",
            MediaType.TEXT_PLAIN_VALUE,
            "Test file content".toByteArray(),
          ),
        )

        // Verify that the order contains only the new pdf
        verify(orderRepo, times(2)).save(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                username = username,
                orderId = mockOrderId,
                additionalDocuments = mutableListOf(
                  AdditionalDocument(
                    id = defaultUuid,
                    versionId = mockVersionId,
                    fileType = docType,
                    fileName = "file-name.pdf",
                  ),
                ),
                dataDictionaryVersion = mockDictionaryVersion,
              ),
            ),
          ),
        )

        // Verify that the old file was deleted from document api
        argumentCaptor<String>().apply {
          verify(client, times(1)).deleteDocument(capture())
          Assertions.assertThat(firstValue).isEqualTo(doc.id.toString())
        }

        // Verify the new file was uploaded to the document api
        argumentCaptor<String, MultipartBodyBuilder>().apply {
          verify(client, times(1)).createDocument(first.capture(), second.capture())
          Assertions.assertThat(first.firstValue).isEqualTo(defaultUuid.toString())
          val multipartBody = second.firstValue.build()
          Assertions.assertThat(multipartBody["file"]?.get(0)).isNotNull
          Assertions.assertThat(
            multipartBody["metadata"]?.get(0)?.body.toString(),
          ).isEqualTo("DocumentMetadata(orderId=$mockOrderId, documentType=LICENCE)")
        }
      }
    }

    @Test
    fun `it should create a new document`() {
      val defaultUuid = UUID.randomUUID()
      Mockito.mockStatic(UUID::class.java).use {
        it.`when`<Any> { UUID.randomUUID() }.thenReturn(defaultUuid)

        // Mock an order with no documents
        whenever(
          orderRepo.findById(mockOrderId),
        ).thenReturn(
          Optional.of(
            Order(
              id = mockOrderId,
              versions = mutableListOf(
                OrderVersion(
                  id = mockVersionId,
                  status = OrderStatus.IN_PROGRESS,
                  type = RequestType.REQUEST,
                  username = username,
                  orderId = mockOrderId,
                  dataDictionaryVersion = mockDictionaryVersion,
                ),

              ),
            ),
          ),
        )

        // Upload a document
        service.uploadDocument(
          mockOrderId,
          username,
          docType,
          MockMultipartFile(
            "file",
            "file-name.pdf",
            MediaType.TEXT_PLAIN_VALUE,
            "Test file content".toByteArray(),
          ),
        )

        // Verify that the order contains only the new pdf
        verify(orderRepo, times(1)).save(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                username = username,
                orderId = mockOrderId,
                additionalDocuments = mutableListOf(
                  AdditionalDocument(
                    id = defaultUuid,
                    versionId = mockVersionId,
                    fileType = docType,
                    fileName = "file-name.pdf",
                  ),
                ),
                dataDictionaryVersion = mockDictionaryVersion,
              ),
            ),
          ),
        )

        // Verify the new file was uploaded to the document api
        argumentCaptor<String, MultipartBodyBuilder>().apply {
          verify(client, times(1)).createDocument(first.capture(), second.capture())
          Assertions.assertThat(first.firstValue).isEqualTo(defaultUuid.toString())
          val multipartBody = second.firstValue.build()
          Assertions.assertThat(multipartBody["file"]?.get(0)).isNotNull
          Assertions.assertThat(
            multipartBody["metadata"]?.get(0)?.body.toString(),
          ).isEqualTo("DocumentMetadata(orderId=$mockOrderId, documentType=LICENCE)")
        }
      }
    }
  }
}
