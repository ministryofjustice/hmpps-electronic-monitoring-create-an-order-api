package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderParameters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateFileRequiredDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateHavePhotoDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.AdditionalDocumentService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class AdditionalDocumentsController(@Autowired val documentService: AdditionalDocumentService) {

  @PostMapping(
    "/orders/{orderId}/document-type/{fileType}",
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun postAdditionalDocument(
    @PathVariable orderId: UUID,
    @PathVariable fileType: DocumentType,
    @RequestPart file: MultipartFile,
    authentication: Authentication,
  ): ResponseEntity<AdditionalDocument> {
    val username = authentication.name

    documentService.uploadDocument(orderId, username, fileType, file)
    return ResponseEntity(HttpStatus.OK)
  }

  @GetMapping("/orders/{orderId}/document-type/{fileType}/raw")
  fun downloadDocument(
    @PathVariable orderId: UUID,
    @PathVariable fileType: DocumentType,
    authentication: Authentication,
  ): ResponseEntity<InputStreamResource> {
    val username = authentication.name
    val documentResponse = documentService.getDocument(orderId, username, fileType)!!
    val fileStream = documentResponse.body?.blockFirst()
    return ResponseEntity.ok()
      .header(
        HttpHeaders.CONTENT_DISPOSITION,
        documentResponse.headers.contentDisposition.toString(),
      )
      .contentType(documentResponse.headers.contentType!!)
      .contentLength(documentResponse.headers.contentLength)
      .body(fileStream)
  }

  @DeleteMapping("/orders/{orderId}/document-type/{fileType}")
  fun deleteDocument(
    @PathVariable orderId: UUID,
    @PathVariable fileType: DocumentType,
    authentication: Authentication,
  ): ResponseEntity<AdditionalDocument> {
    val username = authentication.name

    documentService.deleteDocument(orderId, username, fileType)

    return ResponseEntity(HttpStatus.NO_CONTENT)
  }

  @PutMapping("/orders/{orderId}/attachments/have-photo")
  fun havePhoto(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateHavePhotoDto,
    authentication: Authentication,
  ): ResponseEntity<OrderParameters> {
    val username = authentication.name
    val parameters = documentService.updateHavePhoto(
      orderId,
      username,
      updateRecord,
    )

    return ResponseEntity(parameters, HttpStatus.OK)
  }

  @PutMapping("/orders/{orderId}/attachments/file-required")
  fun fileRequired(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateFileRequiredDto,
    authentication: Authentication,
  ): ResponseEntity<OrderParameters> {
    val username = authentication.name
    val parameters = documentService.updateFileRequired(
      orderId,
      username,
      updateRecord,
    )

    return ResponseEntity(parameters, HttpStatus.OK)
  }
}
