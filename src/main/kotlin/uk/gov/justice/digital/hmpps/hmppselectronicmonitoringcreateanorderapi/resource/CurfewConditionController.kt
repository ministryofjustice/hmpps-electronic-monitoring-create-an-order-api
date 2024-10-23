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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.CurfewConditionService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class CurfewConditionController(val service: CurfewConditionService) {

  @Validated
  @PutMapping("/orders/{orderId}/monitoring-conditions-curfew-conditions")
  fun updateDeviceWearer(
    @PathVariable orderId: UUID,
    @RequestBody @Valid conditions: CurfewConditions,
    authentication: Authentication,
  ): ResponseEntity<CurfewConditions> {
    val username = authentication.name
    service.updateCurfewCondition(orderId, username, conditions)
    return ResponseEntity(conditions, HttpStatus.OK)
  }
}