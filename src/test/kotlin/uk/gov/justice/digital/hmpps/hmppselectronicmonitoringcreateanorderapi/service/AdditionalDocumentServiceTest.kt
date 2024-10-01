package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.AdditionalDocumentRepository
import java.io.ByteArrayInputStream
import java.util.*

@ActiveProfiles("test")
@JsonTest
class AdditionalDocumentServiceTest {
  private lateinit var service: AdditionalDocumentService
  private lateinit var client: DocumentApiClient
  private lateinit var repo: AdditionalDocumentRepository
  val orderId: UUID = UUID.randomUUID()
  val username: String = "username"
  val docType: DocumentType = DocumentType.LICENCE
  val doc: AdditionalDocument = AdditionalDocument(orderId = orderId, fileType = docType)

  @BeforeEach
  fun setup() {
    repo = mock(AdditionalDocumentRepository::class.java)
    client = mock(DocumentApiClient::class.java)
    service = AdditionalDocumentService(repo, client)
  }

  @Test
  fun `document not exist with orderId and document type, throw entity not found error`() {
    val e = assertThrows(EntityNotFoundException::class.java) {
      service.getDocument(orderId, username, docType)
    }
    Assertions.assertThat(e.message).isEqualTo("Document for $orderId with type $docType not found")
  }

  @Test
  fun `document exist should retrieve raw document from document management api`() {
    `when`(repo.findAdditionalDocumentsByOrderIdAndOrderUsernameAndFileType(orderId, username, docType)).thenReturn(Optional.of(doc))
    `when`(client.getDocument(doc.id.toString())).thenReturn(
      ResponseEntity.ok().body(Flux.just(InputStreamResource(ByteArrayInputStream("".toByteArray())))),
    )
    service.getDocument(orderId, username, docType)
    verify(client, times(1)).getDocument(doc.id.toString())
  }

  @Test
  fun `delete document in repo and in document management api`() {
    `when`(repo.findAdditionalDocumentsByOrderIdAndOrderUsernameAndFileType(orderId, username, docType)).thenReturn(Optional.of(doc))

    service.deleteDocument(orderId, username, docType)

    argumentCaptor<UUID>().apply {
      verify(repo, times(1)).deleteById(capture())
      Assertions.assertThat(firstValue).isEqualTo(doc.id)
    }

    argumentCaptor<String>().apply {
      verify(client, times(1)).deleteDocument(capture())
      Assertions.assertThat(firstValue).isEqualTo(doc.id.toString())
    }
  }

  @Test
  fun `document extension not allowed, throw unsupported validation exception`() {
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
    Assertions.assertThat(e.message).isEqualTo("Unsupported or missing file type txt. Supported file types: pdf, jpeg, png")
  }

  @Test
  fun `document with same type already exist, remove old document`() {
    `when`(repo.findAdditionalDocumentsByOrderIdAndOrderUsernameAndFileType(orderId, username, docType)).thenReturn(Optional.of(doc))

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

    argumentCaptor<UUID>().apply {
      verify(repo, times(1)).deleteById(capture())
      Assertions.assertThat(firstValue).isEqualTo(doc.id)
    }

    argumentCaptor<String>().apply {
      verify(client, times(1)).deleteDocument(capture())
      Assertions.assertThat(firstValue).isEqualTo(doc.id.toString())
    }
  }

  @Test
  fun `save document in repo and call document management api`() {
    val defaultUuid = UUID.randomUUID()
    Mockito.mockStatic(UUID::class.java).use {
      it.`when`<Any> { UUID.randomUUID() }.thenReturn(defaultUuid)

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
      argumentCaptor<AdditionalDocument>().apply {
        verify(repo, times(1)).save(capture())
        Assertions.assertThat(firstValue.id).isEqualTo(defaultUuid)
        Assertions.assertThat(firstValue.orderId).isEqualTo(orderId)
        Assertions.assertThat(firstValue.fileType).isEqualTo(docType)
        Assertions.assertThat(firstValue.fileName).isEqualTo("file-name.pdf")
      }
      argumentCaptor<String, MultipartBodyBuilder>().apply {
        verify(client, times(1)).createDocument(first.capture(), second.capture())
        Assertions.assertThat(first.firstValue).isEqualTo(defaultUuid.toString())
        val multipartBody = second.firstValue.build()
        Assertions.assertThat(multipartBody["file"]?.get(0)).isNotNull
        Assertions.assertThat(multipartBody["metadata"]?.get(0)?.body.toString()).isEqualTo("DocumentMetadata(orderId=$orderId, documentType=LICENCE)")
      }
    }
  }
}
