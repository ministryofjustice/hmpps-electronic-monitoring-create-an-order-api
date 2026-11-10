package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.flywaydb.core.Flyway
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.persistenceunit.ManagedClassNameFilter
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3Simulation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationCreation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationReturnEvent

@Configuration(proxyBeanMethods = false)
class Up3SimulationPersistenceConfiguration {
  @Bean
  fun simulationManagedClassNameFilter(settings: Up3SimulationSettings): ManagedClassNameFilter {
    val simulationTypes = setOf(
      Up3Simulation::class.java.name,
      Up3SimulationCreation::class.java.name,
      Up3SimulationReturnEvent::class.java.name,
    )
    return ManagedClassNameFilter { settings.storageAvailable || it !in simulationTypes }
  }

  @Bean
  fun simulationMigrationStrategy(settings: Up3SimulationSettings): FlywayMigrationStrategy =
    FlywayMigrationStrategy { flyway ->
      check(
        flyway.info().all().none { it.physicalLocation?.contains("db/up3-simulation/") == true },
      ) { "UP3 simulation migrations must use the separate dev/local migration runner" }
      flyway.migrate()
      if (settings.storageAvailable) {
        Flyway.configure().configuration(flyway.configuration)
          .locations("classpath:db/up3-simulation")
          .table("flyway_up3_simulation_history")
          .baselineOnMigrate(true)
          .baselineVersion("0")
          .load().migrate()
      }
    }
}
