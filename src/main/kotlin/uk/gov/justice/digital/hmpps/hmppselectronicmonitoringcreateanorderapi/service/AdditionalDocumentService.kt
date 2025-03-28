package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentMetadata
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import java.util.*

@Service
class AdditionalDocumentService(val webClient: DocumentApiClient) : OrderSectionServiceBase() {

  val allowedFileExtensions: List<String> = listOf("pdf", "png", "jpeg", "jpg")

  fun getDocument(
    orderId: UUID,
    username: String,
    documentType: DocumentType,
  ): ResponseEntity<Flux<InputStreamResource>>? {
    val order = this.findEditableOrder(orderId, username)
    val doc = order.additionalDocuments.firstOrNull { it.fileType == documentType }

    if (doc === null) {
      throw EntityNotFoundException("Document for $orderId with type $documentType not found")
    }

    return webClient.getDocument(doc.id.toString())
  }

  fun deleteDocument(orderId: UUID, username: String, documentType: DocumentType) {
    val order = findEditableOrder(orderId, username)
    val doc = order.additionalDocuments.firstOrNull { it.fileType == documentType }
    if (doc != null) {
      order.additionalDocuments.remove(doc)
      orderRepo.save(order)
      webClient.deleteDocument(doc.id.toString())
    }
  }

  fun uploadDocument(orderId: UUID, username: String, documentType: DocumentType, multipartFile: MultipartFile) {
    validateFileExtension(multipartFile)

    deleteDocument(orderId, username, documentType)

    val order = this.findEditableOrder(orderId, username)
    val document =
      AdditionalDocument(
        versionId = order.getCurrentVersion().id,
        fileType = documentType,
        fileName = multipartFile.originalFilename,
      )
    val builder = MultipartBodyBuilder()
    builder.part("file", multipartFile.resource)
      .contentType(MediaType.valueOf(multipartFile.contentType!!))
      .filename(multipartFile.originalFilename!!)
    builder.part("metadata", DocumentMetadata(orderId, documentType))
    webClient.createDocument(document.id.toString(), builder)

    order.additionalDocuments.add(document)

    orderRepo.save(order)
  }

  private fun validateFileExtension(multipartFile: MultipartFile) {
    val extension = FilenameUtils.getExtension(multipartFile.originalFilename)?.lowercase()
    if (!StringUtils.hasLength(extension) || !allowedFileExtensions.contains(extension)
    ) {
      throw ValidationException(
        String.format(
          "Unsupported or missing file type %s. Supported file types: %s",
          extension,
          allowedFileExtensions.joinToString(),
        ),
      )
    }
  }
}
