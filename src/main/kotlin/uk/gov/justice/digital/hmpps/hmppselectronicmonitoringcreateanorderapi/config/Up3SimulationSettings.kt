package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.OrderChangeException

@Component
class Up3SimulationSettings(
  @param:Value("\${settings.up3-simulation-enabled:false}") val enabled: Boolean,
  environment: Environment,
) {
  private val profiles = environment.activeProfiles.toSet()
  val storageAvailable = profiles.any { it in setOf("local", "dev") } &&
    profiles.all { it in setOf("local", "dev", "test") }

  init {
    require(profiles.none { it in setOf("local", "dev") } || storageAvailable) {
      "UP3 simulation storage cannot combine local/dev with other deployment profiles"
    }
    require(!enabled || storageAvailable) {
      "UP3 simulation may only be enabled with local/dev profiles"
    }
    require(!enabled || environment.getProperty("settings.return-listener-enabled", Boolean::class.java, false)) {
      "UP3 simulation requires the returns listener"
    }
  }

  fun requireEnabled() {
    if (!enabled) {
      throw OrderChangeException("UP3_SIMULATION_DISABLED", "This simulated order cannot use real UP3")
    }
  }
}
