package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderParameters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentMetadata
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateHavePhotoDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.validators.FileUploadValidator.validateFileExtensionAndSize
import java.util.*

@Service
class AdditionalDocumentService(val webClient: DocumentApiClient) : OrderSectionServiceBase() {

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
    validateFileExtensionAndSize(multipartFile, documentType)

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

  fun updateHavePhoto(orderId: UUID, username: String, updateRecord: UpdateHavePhotoDto): OrderParameters {
    val order = this.findEditableOrder(orderId, username)

    // Either update current params or create a new record if one does not exist for this order version
    if (order.orderParameters == null) {
      order.orderParameters =
        OrderParameters(versionId = order.getCurrentVersion().id, havePhoto = updateRecord.havePhoto)
    } else {
      order.orderParameters?.havePhoto = updateRecord.havePhoto
    }

    return orderRepo.save(order).orderParameters!!
  }
}
