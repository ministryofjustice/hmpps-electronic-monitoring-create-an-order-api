package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedCreationReceiptDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedCreationStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedOrderState
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "UP3_SIMULATION_CREATION")
class Up3SimulationCreation(
  @Id
  val creationRequestId: UUID,
  @Column(nullable = false, length = 64)
  val payloadHash: String,
  @Column(nullable = false)
  val createdBy: String,
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  val requestedState: SimulatedOrderState,
  @Column(nullable = false)
  val createdAt: Instant = Instant.now(),
  @Column(unique = true)
  var orderId: UUID? = null,
  var createdOrderVersionId: UUID? = null,
  var returnEventId: UUID? = null,
) : Persistable<UUID> {
  @Transient
  private var newReceipt = true

  override fun getId(): UUID = creationRequestId

  override fun isNew(): Boolean = newReceipt

  @PostLoad
  @PostPersist
  fun markPersisted() {
    newReceipt = false
  }

  fun toDto() = SimulatedCreationReceiptDto(
    creationRequestId,
    if (orderId == null) SimulatedCreationStatus.PENDING else SimulatedCreationStatus.CREATED,
    orderId,
    createdOrderVersionId,
    requestedState,
    returnEventId,
  )
}
