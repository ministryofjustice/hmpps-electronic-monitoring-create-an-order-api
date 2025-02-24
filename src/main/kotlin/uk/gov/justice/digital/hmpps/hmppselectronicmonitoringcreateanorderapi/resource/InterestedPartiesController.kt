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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInterestedPartiesDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.InterestedPartiesService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class InterestedPartiesController(@Autowired val interestPartiesService: InterestedPartiesService) {
  @PutMapping("/orders/{orderId}/interested-parties")
  fun updateAddress(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateInterestedPartiesDto,
    authentication: Authentication,
  ): ResponseEntity<InterestedParties> {
    val username = authentication.name
    val result = interestPartiesService.updateInterestedParties(
      orderId,
      username,
      updateRecord,
    )

    return ResponseEntity(result, HttpStatus.OK)
  }
}
