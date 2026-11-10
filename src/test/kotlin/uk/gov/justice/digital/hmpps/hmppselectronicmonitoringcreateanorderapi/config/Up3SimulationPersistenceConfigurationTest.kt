package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.mock.env.MockEnvironment
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3Simulation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationCreation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationReturnEvent
import java.sql.DriverManager
import java.util.UUID

class Up3SimulationPersistenceConfigurationTest {
  private val configuration = Up3SimulationPersistenceConfiguration()
  private val simulationTypes = listOf(
    Up3Simulation::class.java.name,
    Up3SimulationCreation::class.java.name,
    Up3SimulationReturnEvent::class.java.name,
  )

  @Test
  fun `production scans normal entities but not optional simulator entities or migrations`() {
    val settings = settings("prod")
    val managed = PersistenceManagedTypesScanner(
      DefaultResourceLoader(),
      configuration.simulationManagedClassNameFilter(settings),
    ).scan(Order::class.java.packageName)
    assertThat(managed.managedClassNames).contains(Order::class.java.name).doesNotContainAnyElementsOf(simulationTypes)
    val flyway = Flyway.configure().dataSource("jdbc:h2:mem:${UUID.randomUUID()}", "sa", "")
      .locations("classpath:db/migration").load()
    assertThat(flyway.info().all().map { it.script }).noneMatch { it.contains("Up3_Simulation") }
  }

  @Test
  fun `disabled dev controls still include simulator entities`() {
    val filter = configuration.simulationManagedClassNameFilter(settings("dev"))
    assertThat(simulationTypes.all { filter.matches(it) }).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["classpath:db/up3-simulation", "classpath:db"])
  fun `including simulator migrations in the main runner fails before creating tables`(location: String) {
    val url = "jdbc:h2:mem:${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
    val flyway = Flyway.configure().dataSource(url, "sa", "")
      .locations(location).load()
    assertThatThrownBy { configuration.simulationMigrationStrategy(settings("prod")).migrate(flyway) }
      .isInstanceOf(RuntimeException::class.java)
    DriverManager.getConnection(url, "sa", "").use { connection ->
      connection.metaData.getTables(null, "PUBLIC", "%", arrayOf("TABLE")).use {
        assertThat(it.next()).isFalse()
      }
    }
  }

  @Test
  fun `dev migrates persistent simulator tables once even when controls are off`() {
    val url = "jdbc:h2:mem:${UUID.randomUUID()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
    DriverManager.getConnection(url, "sa", "").use {
      it.createStatement().use { sql -> sql.execute("CREATE TABLE orders (id UUID PRIMARY KEY)") }
    }
    val flyway = Flyway.configure().dataSource(url, "sa", "")
      .locations("classpath:db/nonexistent-test-main-migrations")
      .baselineOnMigrate(true).baselineVersion("0").load()
    val strategy = configuration.simulationMigrationStrategy(settings("dev"))
    strategy.migrate(flyway)
    val orderId = UUID.randomUUID()
    DriverManager.getConnection(url, "sa", "").use { connection ->
      connection.createStatement().use {
        it.execute("INSERT INTO orders VALUES ('$orderId')")
        it.execute(
          """
          INSERT INTO up3_simulation (order_id, version_id, case_id, case_state, enrolled_by, enrolled_at)
          VALUES ('$orderId', '${UUID.randomUUID()}', 'sim-test', 'NEW', 'test-user', CURRENT_TIMESTAMP)
          """.trimIndent(),
        )
      }
    }
    strategy.migrate(flyway)
    assertThat(flyway.info().applied().map { it.script }).noneMatch { it.contains("Up3_Simulation") }
    val simulator = Flyway.configure().configuration(flyway.configuration)
      .locations("classpath:db/up3-simulation").table("flyway_up3_simulation_history").load()
    assertThat(simulator.info().applied().map { it.script }).contains(
      "V1__Add_Up3_Simulation.sql",
      "V2__Add_Up3_Simulation_Receipts.sql",
    )
    DriverManager.getConnection(url, "sa", "").use { connection ->
      val tables = connection.metaData.getTables(null, "PUBLIC", "UP3_SIMULATION%", arrayOf("TABLE")).use {
        buildList { while (it.next()) add(it.getString("TABLE_NAME")) }
      }
      assertThat(tables).containsExactlyInAnyOrder(
        "UP3_SIMULATION",
        "UP3_SIMULATION_RETURN_EVENT",
        "UP3_SIMULATION_CREATION",
      )
      connection.createStatement().use { sql ->
        sql.executeQuery("SELECT order_id FROM up3_simulation").use {
          assertThat(it.next()).isTrue()
          assertThat(it.getObject(1, UUID::class.java)).isEqualTo(orderId)
        }
      }
    }
  }

  private fun settings(profile: String): Up3SimulationSettings {
    val env = MockEnvironment()
    env.setActiveProfiles(profile)
    return Up3SimulationSettings(false, env)
  }
}
