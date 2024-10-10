package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EnforcementZoneService
import java.util.UUID

@RestController
// @PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class EnformentZoneController(
  @Autowired val enforcementZoneService: EnforcementZoneService,

) {

  @PostMapping("/order/{orderId}/enforcementZone")
  fun updateEnforcementZone(
    @PathVariable orderId: UUID,
    @RequestBody @Valid enforcementZone: EnforcementZoneConditions,
    authentication: Authentication,
  ): ResponseEntity<AdditionalDocument> {
    val username = authentication.name
    enforcementZoneService.updateEnforcementZone(orderId, username, enforcementZone)
    return ResponseEntity(HttpStatus.OK)
  }

  @PostMapping("/order/{orderId}/enforcementZone/{zoneId}/attachment", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun postAdditionalDocument(
    @PathVariable orderId: UUID,
    @PathVariable zoneId: Int,
    @RequestPart file: MultipartFile,
    authentication: Authentication,
  ): ResponseEntity<AdditionalDocument> {
    val username = authentication.name
    enforcementZoneService.uploadEnforcementZoneAttachment(orderId, username, zoneId, file)
    return ResponseEntity(HttpStatus.OK)
  }
}
