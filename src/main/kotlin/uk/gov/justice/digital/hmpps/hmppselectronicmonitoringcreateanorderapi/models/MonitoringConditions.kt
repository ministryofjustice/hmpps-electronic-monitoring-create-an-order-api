package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.converter.ArrayToStringConverter
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "MONITORING_CONDITIONS")
data class MonitoringConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "START_DATE", nullable = true)
  var startDate: ZonedDateTime? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: ZonedDateTime? = null,

  @Column(name = "ORDER_TYPE", nullable = true)
  var orderType: String? = null,

  @Convert(converter = ArrayToStringConverter::class)
  @Column(name = "DEVICES_REQUIRED", nullable = true)
  var devicesRequired: Array<String>? = null,

  @Column(name = "CASE_ID", nullable = true)
  var caseId: String? = null,

  @Column(name = "CONDITION_TYPE", nullable = true)
  var conditionType: String? = null,

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
  private val order: Order? = null,
)
