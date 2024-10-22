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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.CurfewTimetableService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class CurfewTimetableController(val service: CurfewTimetableService) {

  @Validated
  @PutMapping("/orders/{orderId}/monitoring-conditions-curfew-timetable")
  fun updateDeviceWearer(
    @PathVariable orderId: UUID,
    @RequestBody @Valid timetable: List<CurfewTimeTable>,
    authentication: Authentication,
  ): ResponseEntity<List<CurfewTimeTable>> {
    val username = authentication.name
    service.updateCurfewTimetable(orderId, username, timetable)
    return ResponseEntity(timetable, HttpStatus.OK)
  }
}
