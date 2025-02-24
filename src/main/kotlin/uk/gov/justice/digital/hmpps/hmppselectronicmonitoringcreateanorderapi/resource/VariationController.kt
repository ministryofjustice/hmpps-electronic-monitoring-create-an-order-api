package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateVariationDetailsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.VariationService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class VariationController(@Autowired val variationService: VariationService) {
  @PutMapping("/orders/{orderId}/variation")
  fun updateVariationDetails(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateVariationDetailsDto,
    authentication: Authentication,
  ): ResponseEntity<VariationDetails> {
    val username = authentication.name
    val variationDetails = variationService.updateVariationDetails(
      orderId,
      username,
      updateRecord,
    )

    return ResponseEntity(variationDetails, HttpStatus.OK)
  }
}
