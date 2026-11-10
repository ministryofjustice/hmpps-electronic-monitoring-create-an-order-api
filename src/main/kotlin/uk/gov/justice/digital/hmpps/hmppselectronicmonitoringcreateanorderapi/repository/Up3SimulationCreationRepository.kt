package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationCreation
import java.util.UUID

@Profile("dev", "local")
interface Up3SimulationCreationRepository :
  JpaRepository<Up3SimulationCreation, UUID>,
  Up3SimulationCreationLock

interface Up3SimulationCreationLock {
  fun lockById(id: UUID): Up3SimulationCreation?
}

class Up3SimulationCreationLockImpl(private val entityManager: EntityManager) : Up3SimulationCreationLock {
  override fun lockById(id: UUID): Up3SimulationCreation? {
    val receipt = entityManager.find(Up3SimulationCreation::class.java, id) ?: return null
    // The reservation transaction may have left an older instance in the request's persistence context.
    entityManager.refresh(receipt, LockModeType.PESSIMISTIC_WRITE)
    return receipt
  }
}
