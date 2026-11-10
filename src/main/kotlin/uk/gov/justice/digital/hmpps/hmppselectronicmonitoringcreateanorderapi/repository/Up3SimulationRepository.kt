package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import jakarta.persistence.LockModeType
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3Simulation
import java.util.UUID

@Profile("dev", "local")
interface Up3SimulationRepository : JpaRepository<Up3Simulation, UUID> {
  fun findByCaseId(caseId: String): Up3Simulation?

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Up3Simulation s where s.orderId = :orderId")
  fun lockByOrderId(orderId: UUID): Up3Simulation?
}
