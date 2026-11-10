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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedReturnEventDto
import java.time.Instant
import java.util.UUID

enum class SimulatedReturnStatus {
  PENDING_PUBLICATION,
  QUEUED,
  PROCESSED,
  FAILED,
  STALE_IGNORED,
}

enum class SimulationFailureStage {
  PUBLICATION,
  PROCESSING,
}

@Entity
@Table(name = "UP3_SIMULATION_RETURN_EVENT")
class Up3SimulationReturnEvent(
  @Id
  val eventId: UUID,
  @Column(nullable = false)
  val orderId: UUID,
  @Column(nullable = false)
  val targetOrderVersionId: UUID,
  @Column(nullable = false)
  val simulatedSubmissionVersionId: UUID,
  @Column(nullable = false)
  val caseId: String,
  @Column(nullable = false, length = 64)
  val payloadHash: String,
  @Column(nullable = false, columnDefinition = "text")
  val payload: String,
  @Column(nullable = false)
  val createdAt: Instant = Instant.now(),
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  var status: SimulatedReturnStatus = SimulatedReturnStatus.PENDING_PUBLICATION,
  var queuedAt: Instant? = null,
  var completedAt: Instant? = null,
  @Column(nullable = false)
  var publicationAttempts: Int = 0,
  @Column(nullable = false)
  var processingFailures: Int = 0,
  var nextPublicationAttemptAt: Instant? = createdAt,
  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  var failureStage: SimulationFailureStage? = null,
  var errorCode: String? = null,
) : Persistable<UUID> {
  @Transient
  private var newReceipt = true

  override fun getId(): UUID = eventId

  override fun isNew(): Boolean = newReceipt

  @PostLoad
  @PostPersist
  fun markPersisted() {
    newReceipt = false
  }

  fun toDto() = SimulatedReturnEventDto(
    eventId, orderId, targetOrderVersionId, simulatedSubmissionVersionId, status, createdAt,
    queuedAt, completedAt, publicationAttempts, processingFailures, nextPublicationAttemptAt, failureStage, errorCode,
  )

  fun complete(outcome: SimulatedReturnStatus, code: String? = null) {
    status = outcome
    completedAt = Instant.now()
    nextPublicationAttemptAt = null
    failureStage = null
    errorCode = code
  }
}
