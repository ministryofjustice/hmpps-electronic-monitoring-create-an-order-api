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
  val order = Order(username = username, status = OrderStatus.IN_PROGRESS, type = RequestType.REQUEST)
  val orderId = order.id
  val docType: DocumentType = DocumentType.LICENCE
  val doc: AdditionalDocument = AdditionalDocument(orderId = orderId, fileType = docType)

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
        orderRepo.findByIdAndUsernameAndStatus(orderId, username, OrderStatus.IN_PROGRESS),
      ).thenReturn(
        Optional.of(
          Order(
            id = orderId,
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            username = username,
          ),
        ),
      )

      // Verify that an error is thrown
      val e = assertThrows(EntityNotFoundException::class.java) {
        service.getDocument(orderId, username, docType)
      }

      Assertions.assertThat(e.message).isEqualTo("Document for $orderId with type $docType not found")
    }

    @Test
    fun `it should return the object blob if the document exists`() {
      // Mock an order with a single document
      whenever(
        orderRepo.findByIdAndUsernameAndStatus(orderId, username, OrderStatus.IN_PROGRESS),
      ).thenReturn(
        Optional.of(
          Order(
            id = orderId,
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            username = username,
            additionalDocuments = mutableListOf(
              doc,
            ),
          ),
        ),
      )

      // Mock the document api response
      `when`(client.getDocument(doc.id.toString())).thenReturn(
        ResponseEntity.ok().body(
          Flux.just(InputStreamResource(ByteArrayInputStream("".toByteArray()))),
        ),
      )

      // Get the document
      val result = service.getDocument(orderId, username, docType)

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
        orderRepo.findByIdAndUsernameAndStatus(orderId, username, OrderStatus.IN_PROGRESS),
      ).thenReturn(
        Optional.of(
          Order(
            id = orderId,
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            username = username,
            additionalDocuments = mutableListOf(
              doc,
            ),
          ),
        ),
      )

      // Delete the document
      service.deleteDocument(orderId, username, docType)

      // Verify that the document was removed from the order
      verify(orderRepo, times(1)).save(
        Order(
          id = orderId,
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          username = username,
          additionalDocuments = mutableListOf(),
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
        orderRepo.findByIdAndUsernameAndStatus(orderId, username, OrderStatus.IN_PROGRESS),
      ).thenReturn(
        Optional.of(
          Order(
            id = orderId,
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            username = username,
          ),
        ),
      )

      val e = assertThrows(ValidationException::class.java) {
        service.uploadDocument(
          orderId,
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
          orderRepo.findByIdAndUsernameAndStatus(orderId, username, OrderStatus.IN_PROGRESS),
        ).thenReturn(
          Optional.of(
            Order(
              id = orderId,
              status = OrderStatus.IN_PROGRESS,
              type = RequestType.REQUEST,
              username = username,
              additionalDocuments = mutableListOf(
                doc,
              ),
            ),
          ),
        )

        // Upload a new pdf
        service.uploadDocument(
          orderId,
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
            id = orderId,
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            username = username,
            additionalDocuments = mutableListOf(
              AdditionalDocument(
                id = defaultUuid,
                orderId = orderId,
                fileType = docType,
                fileName = "file-name.pdf",
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
          ).isEqualTo("DocumentMetadata(orderId=$orderId, documentType=LICENCE)")
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
          orderRepo.findByIdAndUsernameAndStatus(orderId, username, OrderStatus.IN_PROGRESS),
        ).thenReturn(
          Optional.of(
            Order(
              id = orderId,
              status = OrderStatus.IN_PROGRESS,
              type = RequestType.REQUEST,
              username = username,
            ),
          ),
        )

        // Upload a document
        service.uploadDocument(
          orderId,
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
            id = orderId,
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            username = username,
            additionalDocuments = mutableListOf(
              AdditionalDocument(
                id = defaultUuid,
                orderId = orderId,
                fileType = docType,
                fileName = "file-name.pdf",
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
          ).isEqualTo("DocumentMetadata(orderId=$orderId, documentType=LICENCE)")
        }
      }
    }
  }
}
