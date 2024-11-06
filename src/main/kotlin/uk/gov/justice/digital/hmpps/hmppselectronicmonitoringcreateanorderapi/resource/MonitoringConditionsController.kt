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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.MonitoringConditionsService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class MonitoringConditionsController(
  @Autowired val monitoringConditionsService: MonitoringConditionsService,
) {
  @PutMapping("/orders/{orderId}/monitoring-conditions")
  fun updateMonitoringConditions(
    @PathVariable orderId: UUID,
    @RequestBody @Valid monitoringConditionsUpdateRecord: UpdateMonitoringConditionsDto,
    authentication: Authentication,
  ): ResponseEntity<MonitoringConditions> {
    val username = authentication.name
    val monitoringConditions = monitoringConditionsService.updateMonitoringConditions(
      orderId,
      username,
      monitoringConditionsUpdateRecord,
    )

    return ResponseEntity(monitoringConditions, HttpStatus.OK)
  }
}
