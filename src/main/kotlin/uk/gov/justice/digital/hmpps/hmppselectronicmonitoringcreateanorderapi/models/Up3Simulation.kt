package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "UP3_SIMULATION")
class Up3Simulation(
  @Id
  @Column(name = "ORDER_ID", nullable = false)
  val orderId: UUID,
  @Column(name = "VERSION_ID", nullable = false)
  var versionId: UUID,
  @Column(name = "CASE_ID", unique = true, nullable = false)
  var caseId: String,
  @Enumerated(EnumType.STRING)
  @Column(name = "CASE_STATE", nullable = false, length = 50)
  var caseState: CaseState = CaseState.NEW,
  @Column(name = "ENROLLED_BY", nullable = false)
  val enrolledBy: String,
  @Column(name = "ENROLLED_AT", nullable = false)
  val enrolledAt: Instant = Instant.now(),
  @Column(name = "LAST_PROCESSED_EVENT_ID")
  var lastProcessedEventId: UUID? = null,
  @Column(name = "CAPTURED_USER_EMAILS", nullable = false)
  var capturedUserEmails: Int = 0,
  @Column(name = "CAPTURED_NO_EMAILS", nullable = false)
  var capturedNoEmails: Int = 0,
)
