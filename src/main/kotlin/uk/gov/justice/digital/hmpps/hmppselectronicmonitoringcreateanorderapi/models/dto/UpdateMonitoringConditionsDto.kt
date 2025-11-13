package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SentenceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import java.time.ZonedDateTime

data class UpdateMonitoringConditionTypeDto(
  var curfew: Boolean? = null,

  val exclusionZone: Boolean? = null,

  val trail: Boolean? = null,

  val mandatoryAttendance: Boolean? = null,

  val alcohol: Boolean? = null,
)

data class UpdateMonitoringConditionsDto(
  @field:NotNull(message = ValidationErrors.MonitoringConditions.ORDER_TYPE_REQUIRED)
  val orderType: OrderType? = null,

  var curfew: Boolean? = null,

  @field:NotNull(message = ValidationErrors.MonitoringConditions.START_DATE_REQUIRED)
  val startDate: ZonedDateTime? = null,

  @field:NotNull(message = ValidationErrors.MonitoringConditions.END_DATE_REQUIRED)
  @field:Future(message = ValidationErrors.MonitoringConditions.END_DATE_MUST_BE_IN_FUTURE)
  val endDate: ZonedDateTime? = null,

  val exclusionZone: Boolean? = null,

  val trail: Boolean? = null,

  val mandatoryAttendance: Boolean? = null,

  val alcohol: Boolean? = null,

  @field:NotNull(message = ValidationErrors.MonitoringConditions.TYPE_REQUIRED)
  val conditionType: MonitoringConditionType? = null,

  val orderTypeDescription: OrderTypeDescription? = null,

  val sentenceType: SentenceType? = null,

  val issp: YesNoUnknown = YesNoUnknown.UNKNOWN,

  val hdc: YesNoUnknown = YesNoUnknown.UNKNOWN,

  val prarr: YesNoUnknown = YesNoUnknown.UNKNOWN,

  val pilot: Pilot? = null,

  val offenceType: String? = "",

  val policeArea: String? = "",

) {
  @AssertTrue(message = ValidationErrors.MonitoringConditions.END_DATE_MUST_BE_AFTER_START_DATE)
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate > this.startDate
    }
    return true
  }
}
