package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import jakarta.persistence.LockModeType
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulatedReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationReturnEvent
import java.time.Instant
import java.util.UUID

@Profile("dev", "local")
interface Up3SimulationReturnEventRepository : JpaRepository<Up3SimulationReturnEvent, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Up3SimulationReturnEvent e where e.eventId = :id")
  fun lockById(@Param("id") id: UUID): Up3SimulationReturnEvent?

  fun findTop20ByStatusInAndNextPublicationAttemptAtLessThanEqualOrderByCreatedAtAsc(
    statuses: Collection<SimulatedReturnStatus>,
    now: Instant,
  ): List<Up3SimulationReturnEvent>
}
