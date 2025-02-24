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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MandatoryAttendanceConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMandatoryAttendanceDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.MandatoryAttendanceService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class MandatoryAttendanceController(@Autowired private val mandatoryAttendanceService: MandatoryAttendanceService) {
  @PutMapping("/orders/{orderId}/mandatory-attendance")
  fun updateMandatoryAttendance(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateMandatoryAttendanceDto,
    authentication: Authentication,
  ): ResponseEntity<MandatoryAttendanceConditions> {
    val username = authentication.name
    val saved = mandatoryAttendanceService.updateMandatoryAttendance(orderId, username, updateRecord)

    return ResponseEntity(saved, HttpStatus.OK)
  }
}
