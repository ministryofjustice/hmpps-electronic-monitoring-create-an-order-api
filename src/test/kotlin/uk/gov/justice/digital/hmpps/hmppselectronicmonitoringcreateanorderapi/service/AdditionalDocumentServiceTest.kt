package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderParameters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateFileRequiredDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateHavePhotoDto
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
  var mockDocumentId = UUID.randomUUID()

  val docType: DocumentType = DocumentType.LICENCE
  val doc: AdditionalDocument =
    AdditionalDocument(versionId = mockVersionId, fileType = docType, documentId = mockDocumentId)

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
      verify(client, times(1)).getDocument(doc.documentId.toString())
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
        Assertions.assertThat(firstValue).isEqualTo(doc.documentId.toString())
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
      ).isEqualTo("Select a PDF or Word document")
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
                    documentId = defaultUuid,
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
          Assertions.assertThat(firstValue).isEqualTo(doc.documentId.toString())
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
                    documentId = defaultUuid,
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

  @Nested
  @DisplayName("havePhoto")
  inner class HavePhoto {
    @Test
    fun `should create a new order parameters if one does not exist`() {
      val mockOrder = Order(
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
      )
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(
          mockOrder,
        ),
      )
      whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

      val result = service.updateHavePhoto(
        mockOrderId,
        username,
        updateRecord = UpdateHavePhotoDto(havePhoto = true),
      )
      Assertions.assertThat(result.havePhoto).isEqualTo(true)
      Assertions.assertThat(result.versionId).isEqualTo(mockVersionId)
    }

    @Test
    fun `should update order parameters if they already exist`() {
      val mockOrder = Order(
        id = mockOrderId,
        versions = mutableListOf(
          OrderVersion(
            id = mockVersionId,
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            username = username,
            orderId = mockOrderId,
            dataDictionaryVersion = mockDictionaryVersion,
            orderParameters = OrderParameters(havePhoto = false, versionId = mockVersionId),
          ),
        ),
      )
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(
          mockOrder,
        ),
      )
      whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

      val result = service.updateHavePhoto(
        mockOrderId,
        username,
        updateRecord = UpdateHavePhotoDto(havePhoto = true),
      )
      Assertions.assertThat(result.havePhoto).isEqualTo(true)
      Assertions.assertThat(result.versionId).isEqualTo(mockVersionId)
    }

    @Test
    fun `should delete photo if have photo is set to false`() {
      val mockOrder = Order(
        id = mockOrderId,
        versions = mutableListOf(
          OrderVersion(
            id = mockVersionId,
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            username = username,
            orderId = mockOrderId,
            dataDictionaryVersion = mockDictionaryVersion,
            orderParameters = OrderParameters(havePhoto = true, versionId = mockVersionId),
            additionalDocuments = mutableListOf(
              AdditionalDocument(
                versionId = mockVersionId,
                fileType = DocumentType.PHOTO_ID,
                documentId = mockDocumentId,
              ),
              AdditionalDocument(
                versionId = mockVersionId,
                fileType = DocumentType.LICENCE,
                documentId = mockDocumentId,
              ),
            ),
          ),
        ),
      )
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(
          mockOrder,
        ),
      )
      whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

      val result = service.updateHavePhoto(
        mockOrderId,
        username,
        updateRecord = UpdateHavePhotoDto(havePhoto = false),
      )
      Assertions.assertThat(result.havePhoto).isEqualTo(false)
      Assertions.assertThat(result.versionId).isEqualTo(mockVersionId)
      Assertions.assertThat(mockOrder.additionalDocuments.size).isEqualTo(1)
      Assertions.assertThat(mockOrder.additionalDocuments.filter { x -> x.fileType == DocumentType.PHOTO_ID }.size)
        .isEqualTo(0)
    }
  }

  @ParameterizedTest(name = "Type: {0}")
  @MethodSource("expectedFileRequiredTypeParameters")
  fun `should create a new order parameters if one does not exist`(fileType: DocumentType) {
    val mockOrder = Order(
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
    )
    whenever(
      orderRepo.findById(mockOrderId),
    ).thenReturn(
      Optional.of(
        mockOrder,
      ),
    )
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val result = service.updateFileRequired(
      mockOrderId,
      username,
      updateRecord = UpdateFileRequiredDto(fileRequired = true, fileType = fileType),
    )
    if (fileType == DocumentType.PHOTO_ID) {
      Assertions.assertThat(result.havePhoto).isEqualTo(true)
    }
    if (fileType == DocumentType.COURT_ORDER) {
      Assertions.assertThat(result.haveCourtOrder).isEqualTo(true)
    }
    if (fileType == DocumentType.GRANT_OF_BAIL) {
      Assertions.assertThat(result.haveGrantOfBail).isEqualTo(true)
    }
    Assertions.assertThat(result.versionId).isEqualTo(mockVersionId)
  }

  @ParameterizedTest(name = "Type: {0}")
  @MethodSource("expectedFileRequiredTypeParameters")
  fun `should update order parameters if they already exist`(fileType: DocumentType) {
    val mockOrder = Order(
      id = mockOrderId,
      versions = mutableListOf(
        OrderVersion(
          id = mockVersionId,
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          username = username,
          orderId = mockOrderId,
          dataDictionaryVersion = mockDictionaryVersion,
          orderParameters = OrderParameters(
            versionId = mockVersionId,
            haveCourtOrder = if (fileType == DocumentType.COURT_ORDER) false else null,
            haveGrantOfBail = if (fileType == DocumentType.GRANT_OF_BAIL) false else null,
            havePhoto = if (fileType == DocumentType.PHOTO_ID) false else null,
          ),
        ),
      ),
    )
    whenever(
      orderRepo.findById(mockOrderId),
    ).thenReturn(
      Optional.of(
        mockOrder,
      ),
    )
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val result = service.updateFileRequired(
      mockOrderId,
      username,
      updateRecord = UpdateFileRequiredDto(fileRequired = true, fileType = fileType),
    )
    if (fileType == DocumentType.PHOTO_ID) {
      Assertions.assertThat(result.havePhoto).isEqualTo(true)
    }
    if (fileType == DocumentType.COURT_ORDER) {
      Assertions.assertThat(result.haveCourtOrder).isEqualTo(true)
    }
    if (fileType == DocumentType.GRANT_OF_BAIL) {
      Assertions.assertThat(result.haveGrantOfBail).isEqualTo(true)
    }
    Assertions.assertThat(result.versionId).isEqualTo(mockVersionId)
  }

  @ParameterizedTest(name = "Type: {0}")
  @MethodSource("expectedFileRequiredTypeParameters")
  fun `should delete document if file exists is set to false`(fileType: DocumentType) {
    val mockOrder = Order(
      id = mockOrderId,
      versions = mutableListOf(
        OrderVersion(
          id = mockVersionId,
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          username = username,
          orderId = mockOrderId,
          dataDictionaryVersion = mockDictionaryVersion,
          orderParameters = OrderParameters(
            versionId = mockVersionId,
            haveCourtOrder = if (fileType == DocumentType.COURT_ORDER) true else null,
            haveGrantOfBail = if (fileType == DocumentType.GRANT_OF_BAIL) true else null,
            havePhoto = if (fileType == DocumentType.PHOTO_ID) true else null,
          ),
          additionalDocuments = mutableListOf(
            AdditionalDocument(
              versionId = mockVersionId,
              fileType = fileType,
              documentId = mockDocumentId,
            ),
            AdditionalDocument(
              versionId = mockVersionId,
              fileType = DocumentType.LICENCE,
              documentId = mockDocumentId,
            ),
          ),
        ),
      ),
    )
    whenever(
      orderRepo.findById(mockOrderId),
    ).thenReturn(
      Optional.of(
        mockOrder,
      ),
    )
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val result = service.updateFileRequired(
      mockOrderId,
      username,
      updateRecord = UpdateFileRequiredDto(fileRequired = false, fileType = fileType),
    )
    if (fileType == DocumentType.PHOTO_ID) {
      Assertions.assertThat(result.havePhoto).isEqualTo(false)
    }
    if (fileType == DocumentType.COURT_ORDER) {
      Assertions.assertThat(result.haveCourtOrder).isEqualTo(false)
    }
    if (fileType == DocumentType.GRANT_OF_BAIL) {
      Assertions.assertThat(result.haveGrantOfBail).isEqualTo(false)
    }
    Assertions.assertThat(result.versionId).isEqualTo(mockVersionId)
    Assertions.assertThat(mockOrder.additionalDocuments.size).isEqualTo(1)
    Assertions.assertThat(mockOrder.additionalDocuments.filter { x -> x.fileType == fileType }.size)
      .isEqualTo(0)
  }

  companion object {
    @JvmStatic
    fun expectedFileRequiredTypeParameters() = listOf(
      Arguments.of(DocumentType.COURT_ORDER),
      Arguments.of(DocumentType.GRANT_OF_BAIL),
      Arguments.of(DocumentType.PHOTO_ID),
    )
  }
}
