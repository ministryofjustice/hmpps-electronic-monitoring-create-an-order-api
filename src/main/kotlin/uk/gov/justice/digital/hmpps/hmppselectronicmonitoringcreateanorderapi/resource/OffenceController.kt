package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OffenceService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class OffenceController(val service: OffenceService) {

  @PutMapping("/orders/{orderId}/offence")
  fun updateDapo(
    @PathVariable orderId: UUID,
    @RequestBody @Valid dto: UpdateOffenceDto,
    authentication: Authentication,
  ): ResponseEntity<Offence> {
    val username = authentication.name
    val offence = service.addOffence(
      orderId,
      username,
      dto,
    )

    return ResponseEntity(offence, HttpStatus.OK)
  }
}
