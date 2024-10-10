package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.PostLoad
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "MONITORING_CONDITIONS")
data class MonitoringConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "ORDER_TYPE", nullable = true)
  var orderType: String? = null,

  @Column(name = "DEVICES_REQUIRED", nullable = true)
  var devicesRequiredString: String? = null,

  @Column(name = "ACQUISITIVE_CRIME", nullable = true)
  var acquisitiveCrime: Boolean? = null,

  @Column(name = "DAPOL", nullable = true)
  var dapol: Boolean? = null,

  @Column(name = "CURFEW", nullable = true)
  var curfew: Boolean? = null,

  @Column(name = "EXCLUSION_ZONE", nullable = true)
  var exclusionZone: Boolean? = null,

  @Column(name = "TRAIL", nullable = true)
  var trail: Boolean? = null,

  @Column(name = "MANDATORY_ATTENDANCE", nullable = true)
  var mandatoryAttendance: Boolean? = null,

  @Column(name = "ALCOHOL", nullable = true)
  var alcohol: Boolean? = null,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: OrderForm? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "monitoringConditions", orphanRemoval = true)
  var trailMonitoringConditions: TrailMonitoringConditions? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "monitoringConditions", orphanRemoval = true)
  var exclusionZoneConditions: MutableList<ExclusionZoneConditions> = mutableListOf(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "monitoringConditions", orphanRemoval = true)
  var mandatoryAttendanceConditions: MutableList<MandatoryAttendanceConditions>? = mutableListOf(),

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "monitoringConditions", orphanRemoval = true)
  var curfewReleaseDateConditions: CurfewReleaseDateConditions? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "monitoringConditions", orphanRemoval = true)
  var curfewConditions: CurfewConditions? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "monitoringConditions", orphanRemoval = true)
  var alcoholMonitoringConditions: AlcoholMonitoringConditions? = null,

  @Transient
  var devicesRequired: Array<String>? = null,
) {
  @PrePersist
  @PreUpdate
  fun devicesRequiredToString() {
    devicesRequiredString = devicesRequired?.joinToString(", ")
  }

  @PostLoad
  fun devicesRequiredToArray() {
    devicesRequired = devicesRequiredString?.split(", ")?.toTypedArray()
  }
}
