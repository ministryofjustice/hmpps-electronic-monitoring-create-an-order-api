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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateAlcoholMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.MonitoringConditionsAlcoholService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class MonitoringConditionsAlcoholController(
  @Autowired val monitoringConditionsAlcoholService: MonitoringConditionsAlcoholService,
) {
  @PutMapping("/orders/{orderId}/monitoring-conditions-alcohol")
  fun updateAlcoholMonitoringConditions(
    @PathVariable orderId: UUID,
    @RequestBody @Valid alcoholMonitoringConditionsUpdateRecord: UpdateAlcoholMonitoringConditionsDto,
    authentication: Authentication,
  ): ResponseEntity<AlcoholMonitoringConditions> {
    val username = authentication.name
    val alcoholMonitoringConditions = monitoringConditionsAlcoholService.createOrUpdateAlcoholMonitoringConditions(
      orderId,
      username,
      alcoholMonitoringConditionsUpdateRecord,
    )

    return ResponseEntity(alcoholMonitoringConditions, HttpStatus.OK)
  }
}
