package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import java.util.UUID

@Entity
@Table(name = "ORDERS")
data class Order(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "USER_NAME", nullable = false)
  var username: String,

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  var status: OrderStatus,

  @Column(name = "FMS_DEVICE_WEARER_ID", nullable = true)
  var fmsDeviceWearerId: String? = null,

  @Column(name = "FMS_MONITORING_ORDER_ID", nullable = true)
  var fmsMonitoringOrderId: String? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var deviceWearer: DeviceWearer? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var deviceWearerResponsibleAdult: ResponsibleAdult? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var responsibleOfficer: ResponsibleOfficer? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var deviceWearerContactDetails: DeviceWearerContactDetails? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var addresses: MutableList<Address> = mutableListOf(),

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var monitoringConditions: MonitoringConditions? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var monitoringConditionsTrail: TrailMonitoringConditions? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var installationAndRisk: InstallationAndRisk? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var additionalDocuments: MutableList<AdditionalDocument> = mutableListOf(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var enforcementZoneConditions: MutableList<EnforcementZoneConditions> = mutableListOf(),

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var alcoholMonitoringConditions: AlcoholMonitoringConditions? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var mandatoryAttendanceConditions: MutableList<MandatoryAttendanceConditions>? = mutableListOf(),

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var curfewReleaseDateConditions: CurfewReleaseDateConditions? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var curfewConditions: CurfewConditions? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var curfewTimeTable: MutableList<CurfewTimeTable> = mutableListOf(),

)
