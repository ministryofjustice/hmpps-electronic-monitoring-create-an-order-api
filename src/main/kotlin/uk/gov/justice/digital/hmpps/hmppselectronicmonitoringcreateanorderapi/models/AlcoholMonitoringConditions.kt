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
@Table(name = "ALCOHOL_MONITORING")
data class AlcoholMonitoringConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "MONITORING_CONDITION_ID", nullable = false, unique = true)
  val monitoringConditionsId: UUID,

  @Column(name = "START_DATE", nullable = true)
  var startDate: LocalDate? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: LocalDate? = null,

  @Column(name = "INSTALLATION_DATE", nullable = true)
  var installationDate: LocalDate? = null,

  @Column(name = "INSTALLATION_TIME", nullable = true)
  var installationTime: String? = null,

  @Column(name = "INSTALLTION_ADDRESS", nullable = true)
  var installationAddress: UUID? = null,

  @Column(name = "INSTALLTION_PLACE", nullable = true)
  var installationPlace: String? = null,

  @OneToOne
  @JoinColumn(name = "MONITORING_CONDITION_ID", updatable = false, insertable = false)
  private val monitoringConditions: MonitoringConditions? = null,
)