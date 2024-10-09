package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.AdditionalDocumentService
import java.util.UUID

@RestController
// @PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class EnformentZoneController(
  @Autowired val documentService: AdditionalDocumentService,
) {

  @PostMapping("/order/{orderId}/enforcementZone", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun updateEnforcementZone(
    @PathVariable orderId: UUID,
    @RequestPart file: MultipartFile,
    @RequestPart("metadata") enforcementZone: EnforcementZoneConditions,
    authentication: Authentication,
  ): ResponseEntity<AdditionalDocument> {
    val username = authentication.name

    // documentService.uploadDocument(orderId, username, fileType, file)
    return ResponseEntity(HttpStatus.OK)
  }
}
