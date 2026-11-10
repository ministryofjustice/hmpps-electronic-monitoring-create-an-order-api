package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.Up3SimulationSettings
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.Up3SimulationAvailabilityDto

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
class Up3SimulationAvailabilityController(private val settings: Up3SimulationSettings) {
  @GetMapping("/api/dev/up3-simulation/availability")
  fun availability() = Up3SimulationAvailabilityDto(
    settings.enabled,
    if (settings.enabled) "UP3_SIMULATION_AVAILABLE" else "UP3_SIMULATION_DISABLED",
  )
}
