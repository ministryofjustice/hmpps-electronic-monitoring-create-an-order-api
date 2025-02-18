
package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateCurfewReleaseDateConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.CurfewReleaseDateService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class CurfewReleaseDateController(val service: CurfewReleaseDateService) {

  @Validated
  @PutMapping("/orders/{orderId}/monitoring-conditions-curfew-release-date")
  fun updateCurfewReleaseDateConditions(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateCurfewReleaseDateConditionsDto,
    authentication: Authentication,
  ): ResponseEntity<CurfewReleaseDateConditions> {
    val username = authentication.name
    val conditions = service.updateCurfewReleaseDateCondition(
      orderId,
      username,
      updateRecord,
    )

    return ResponseEntity(conditions, HttpStatus.OK)
  }
}
