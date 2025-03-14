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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateTrailMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.MonitoringConditionsTrailService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class MonitoringConditionsTrailController(
  @Autowired val monitoringConditionsTrailService: MonitoringConditionsTrailService,
) {
  @PutMapping("/orders/{orderId}/monitoring-conditions-trail")
  fun updateTrailMonitoringConditions(
    @PathVariable orderId: UUID,
    @RequestBody @Valid trailMonitoringConditionsUpdateRecord: UpdateTrailMonitoringConditionsDto,
    authentication: Authentication,
  ): ResponseEntity<TrailMonitoringConditions> {
    val username = authentication.name
    val trailMonitoringConditions = monitoringConditionsTrailService.updateTrailMonitoringConditions(
      orderId,
      username,
      trailMonitoringConditionsUpdateRecord,
    )

    return ResponseEntity(trailMonitoringConditions, HttpStatus.OK)
  }
}
