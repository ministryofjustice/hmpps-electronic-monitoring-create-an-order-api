package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.*
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "MONITORING_CONDITIONS")
data class MonitoringConditions(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = true)
  val versionId: UUID,

  @Column(name = "START_DATE", nullable = true)
  var startDate: ZonedDateTime? = null,

  @Column(name = "END_DATE", nullable = true)
  var endDate: ZonedDateTime? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "ORDER_TYPE", nullable = true)
  var orderType: OrderType? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "ORDER_TYPE_DESCRIPTION", nullable = true)
  var orderTypeDescription: OrderTypeDescription? = null,

  @Column(name = "CASE_ID", nullable = true)
  var caseId: String? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "CONDITION_TYPE", nullable = true)
  var conditionType: MonitoringConditionType? = null,

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

  @Enumerated(EnumType.STRING)
  @Column(name = "SENTENCE_TYPE", nullable = true)
  var sentenceType: SentenceType? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "ISSP", nullable = true)
  var issp: YesNoUnknown? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "HDC", nullable = true)
  var hdc: YesNoUnknown? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "PRARR", nullable = true)
  var prarr: YesNoUnknown? = null,

  @Column(name = "PILOT", nullable = true)
  var pilot: String? = null,

  @Schema(hidden = true)
  @OneToOne
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
) {
  val isValid: Boolean
    get() = (
      orderType != null &&
        (
          curfew == true ||
            exclusionZone == true ||
            trail == true ||
            mandatoryAttendance == true ||
            alcohol == true
          )
      )

  val isCurfewOnlyMonitoringConditions: Boolean
    get() = (
      (
        curfew == true &&
          trail != true &&
          exclusionZone != true &&
          alcohol != true &&
          mandatoryAttendance != true
        )
      )
}
