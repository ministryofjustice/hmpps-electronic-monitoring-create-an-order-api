package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulatedReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationReturnEventRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationReturnPublisher
import java.time.Instant

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = ["settings.up3-simulation-enabled"], havingValue = "true")
class Up3SimulationDispatchConfiguration(
  private val events: Up3SimulationReturnEventRepository,
  private val publisher: Up3SimulationReturnPublisher,
) {
  @Scheduled(fixedDelayString = "\${settings.up3-simulation-publish-delay-ms:1000}")
  fun publishPendingReturns() {
    events.findTop20ByStatusInAndNextPublicationAttemptAtLessThanEqualOrderByCreatedAtAsc(
      listOf(SimulatedReturnStatus.PENDING_PUBLICATION, SimulatedReturnStatus.FAILED),
      Instant.now(),
    ).forEach { publisher.publish(it.eventId) }
  }
}
