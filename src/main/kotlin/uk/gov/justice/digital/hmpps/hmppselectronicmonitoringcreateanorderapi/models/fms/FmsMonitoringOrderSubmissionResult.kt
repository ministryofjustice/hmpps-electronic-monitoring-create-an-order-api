package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import java.util.*

@Entity
@Table(name = "FMS_MONITORING_ORDER_SUBMISSION_RESULT")
data class FmsMonitoringOrderSubmissionResult(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  val status: SubmissionStatus = SubmissionStatus.NOT_SUBMITTED,

  @Column(name = "PAYLOAD", nullable = false, columnDefinition = "varchar(409600)")
  val payload: String = "",

  @Column(name = "MONITORING_ORDER_ID", nullable = false)
  val monitoringOrderId: String = "",

  @Column(name = "ERROR", nullable = false)
  val error: String = "",
)
