package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "CURFEW")
data class CurfewConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "MONITORING_CONDITION_ID", nullable = false, unique = true)
  val monitoringConditionsId: UUID,

  @Column(name = "START_DATE", nullable = true)
  var startDate: ZonedDateTime? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: ZonedDateTime? = null,

  @Column(name = "CURFEW_ADDRESS", nullable = true)
  var curfewAddress: UUID? = null,

  @OneToOne
  @JoinColumn(name = "MONITORING_CONDITION_ID", updatable = false, insertable = false)
  private val monitoringConditions: MonitoringConditions? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "conditions", orphanRemoval = true)
  var curfewTimeTable: MutableList<CurfewTimeTable> = mutableListOf(),
)
