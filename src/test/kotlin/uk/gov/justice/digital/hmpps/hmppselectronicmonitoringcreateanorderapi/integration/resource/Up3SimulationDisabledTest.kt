package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3Simulation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.util.UUID

@ActiveProfiles("dev")
@TestPropertySource(
  properties = [
    "spring.datasource.url=jdbc:h2:mem:simulation-disabled;MODE=PostgreSQL",
    "hmpps.sqs.queues.returnseventqueue.queueName=simulation_disabled_returns_events_queue.fifo",
    "hmpps.sqs.queues.returnseventqueue.queueArn=arn:aws:sqs:eu-west-2:000000000000:simulation_disabled_returns_events_queue.fifo",
    "hmpps.sqs.queues.returnseventqueue.dlqName=simulation_disabled_returns_dead_letter_queue.fifo",
  ],
)
class Up3SimulationDisabledTest : IntegrationTestBase() {
  @Autowired
  lateinit var simulations: Up3SimulationRepository

  @Test
  fun `disabled enrolled submissions fail closed and preserve the normal error status transition`() {
    val order = repo.save(TestUtilities.createReadyToSubmitOrder(username = testUser))
    simulations.save(Up3Simulation(order.id, order.versionId, "sim-${order.versionId}", enrolledBy = testUser))
    webTestClient.post().uri("/api/orders/${order.id}/submit").headers(setAuthorisation())
      .exchange().expectStatus().isBadRequest
    assertThat(repo.findById(order.id).orElseThrow().status).isEqualTo(OrderStatus.ERROR)
    assertThat(sercoApi.allServeEvents).isEmpty()
    assertThat(sercoAuthApi.allServeEvents).isEmpty()
  }

  @Test
  fun `simulation controls do not exist with the default configuration`() {
    webTestClient.post().uri("/api/dev/up3-simulation/orders/${UUID.randomUUID()}")
      .headers(setAuthorisation())
      .exchange().expectStatus().isNotFound
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_DISABLED")
    webTestClient.post().uri("/api/dev/up3-simulation/orders")
      .headers(setAuthorisation())
      .exchange().expectStatus().isNotFound
  }

  @Test
  fun `availability reports disabled but retains normal permissions`() {
    webTestClient.get().uri("/api/dev/up3-simulation/availability")
      .headers(setAuthorisation()).exchange().expectStatus().isOk
      .expectBody().jsonPath("$.available").isEqualTo(false)
      .jsonPath("$.code").isEqualTo("UP3_SIMULATION_DISABLED")
    webTestClient.get().uri("/api/dev/up3-simulation/availability")
      .headers(setAuthorisation(roles = emptyList())).exchange().expectStatus().isForbidden
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_PERMISSION_DENIED")
  }

  @Test
  fun `disabled simulation still requires normal authentication`() {
    webTestClient.post().uri("/api/dev/up3-simulation/orders")
      .exchange().expectStatus().isForbidden
  }
}
