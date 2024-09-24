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
@Table(name = "CURFEW_RELEASE_DATE")
data class CurfewReleaseDateConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "MONITORING_CONDITION_ID", nullable = false, unique = true)
  val monitoringConditionsId: UUID,

  @Column(name = "RELEASE_DATE", nullable = true)
  var releaseDate: LocalDate? = null,

  @Column(name = "START_TIME", nullable = true)
  var startTime: String? = null,

  @Column(name = "END_TIME", nullable = true)
  var endTime: String? = null,

  @Column(name = "CURFEW_ADDRESS", nullable = true)
  var curfewAddress: UUID? = null,

  @OneToOne
  @JoinColumn(name = "MONITORING_CONDITION_ID", updatable = false, insertable = false)
  private val monitoringConditions: MonitoringConditions? = null,
)
