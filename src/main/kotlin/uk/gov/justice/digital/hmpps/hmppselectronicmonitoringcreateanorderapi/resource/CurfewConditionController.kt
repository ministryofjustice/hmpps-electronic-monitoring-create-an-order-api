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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateCurfewAdditionalDetailsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateCurfewConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.CurfewConditionService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class CurfewConditionController(val service: CurfewConditionService) {

  @Validated
  @PutMapping("/orders/{orderId}/monitoring-conditions-curfew-conditions")
  fun updateCurfewConditions(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateCurfewConditionsDto,
    authentication: Authentication,
  ): ResponseEntity<CurfewConditions> {
    val username = authentication.name
    val conditions = service.updateCurfewCondition(
      orderId,
      username,
      updateRecord,
    )

    return ResponseEntity(conditions, HttpStatus.OK)
  }

  @Validated
  @PutMapping("/orders/{orderId}/monitoring-conditions-curfew-additional-details")
  fun updateCurfewDetails(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateCurfewAdditionalDetailsDto,
    authentication: Authentication,
  ): ResponseEntity<CurfewConditions> {
    val username = authentication.name
    val conditions = service.updateCurfewAdditionalDetails(
      orderId,
      username,
      updateRecord,
    )

    return ResponseEntity(conditions, HttpStatus.OK)
  }
}
