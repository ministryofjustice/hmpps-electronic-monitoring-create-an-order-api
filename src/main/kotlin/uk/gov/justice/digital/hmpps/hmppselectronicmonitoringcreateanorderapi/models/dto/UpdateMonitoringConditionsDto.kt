package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.AtLeastOneSelected
import java.time.ZonedDateTime

@AtLeastOneSelected(
  fieldNames = ["curfew", "exclusionZone", "trail", "mandatoryAttendance", "alcohol"],
  message = ValidationErrors.MonitoringConditions.ORDER_TYPE_REQUIRED,
)
data class UpdateMonitoringConditionsDto(
  @field:NotNull(message = ValidationErrors.MonitoringConditions.ORDER_TYPE_REQUIRED)
  val orderType: OrderType? = null,

  var curfew: Boolean? = null,

  @field:NotNull(message = ValidationErrors.MonitoringConditions.START_DATE_REQUIRED)
  var startDate: ZonedDateTime? = null,

  var endDate: ZonedDateTime? = null,

  var exclusionZone: Boolean? = null,

  var trail: Boolean? = null,

  var mandatoryAttendance: Boolean? = null,

  var alcohol: Boolean? = null,

  @field:NotNull(message = ValidationErrors.MonitoringConditions.TYPE_REQUIRED)
  var conditionType: MonitoringConditionType? = null,

  val orderTypeDescription: OrderTypeDescription? = null,
) {
  @AssertTrue(message = ValidationErrors.MonitoringConditions.END_DATE_MUST_BE_AFTER_START_DATE)
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate!! > this.startDate
    }
    return true
  }
}
