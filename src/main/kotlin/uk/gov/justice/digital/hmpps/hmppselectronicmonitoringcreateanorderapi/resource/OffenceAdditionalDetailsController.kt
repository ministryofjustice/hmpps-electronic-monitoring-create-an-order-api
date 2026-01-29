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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OffenceAdditionalDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceAdditionalDetailsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OffenceAdditionalDetailsService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class OffenceAdditionalDetailsController(val service: OffenceAdditionalDetailsService) {

  @PutMapping("/orders/{orderId}/offence-additional-details")
  fun updateOffenceDetails(
    @PathVariable orderId: UUID,
    @RequestBody @Valid dto: UpdateOffenceAdditionalDetailsDto,
    authentication: Authentication,
  ): ResponseEntity<OffenceAdditionalDetails> {
    val username = authentication.name
    val offenceDetails = service.updateOffenceAdditionalDetails(orderId, username, dto)

    return ResponseEntity(offenceDetails, HttpStatus.OK)
  }
}
