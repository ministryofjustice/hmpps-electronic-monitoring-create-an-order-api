package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationCreationRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationReturnEventRepository

class Up3SimulationNoStorageTest : IntegrationTestBase() {
  @Autowired
  lateinit var context: ApplicationContext

  @Autowired
  lateinit var entityManagerFactory: EntityManagerFactory

  @Autowired
  lateinit var jdbc: JdbcTemplate

  @Test
  fun `outside dev normal orders work without any simulator entities repositories or tables`() {
    assertThat(context.getBeansOfType(Up3SimulationRepository::class.java)).isEmpty()
    assertThat(context.getBeansOfType(Up3SimulationReturnEventRepository::class.java)).isEmpty()
    assertThat(context.getBeansOfType(Up3SimulationCreationRepository::class.java)).isEmpty()
    assertThat(entityManagerFactory.metamodel.entities.map { it.name }).doesNotContain(
      "Up3Simulation",
      "Up3SimulationReturnEvent",
      "Up3SimulationCreation",
    )
    assertThat(
      jdbc.queryForList(
        "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'UP3_SIMULATION%'",
        String::class.java,
      ),
    ).isEmpty()
    val order = createOrder()
    assertThat(getOrder(order.id).id).isEqualTo(order.id)
    webTestClient.get().uri("/api/dev/up3-simulation/availability").headers(setAuthorisation())
      .exchange().expectStatus().isOk.expectBody().jsonPath("$.available").isEqualTo(false)
  }
}
