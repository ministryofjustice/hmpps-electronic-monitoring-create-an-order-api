package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "TRAIL_MONITORING")
data class TrailMonitoringConditions(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "MONITORING_CONDITION_ID", nullable = false, unique = true)
  val monitoringConditionsId: UUID,

  @Column(name = "START_DATE", nullable = true)
  var startDate: LocalDate? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: LocalDate? = null,

  @OneToOne
  @JoinColumn(name = "MONITORING_CONDITION_ID", updatable = false, insertable = false)
  private val monitoringConditions: MonitoringConditions? = null,
)
