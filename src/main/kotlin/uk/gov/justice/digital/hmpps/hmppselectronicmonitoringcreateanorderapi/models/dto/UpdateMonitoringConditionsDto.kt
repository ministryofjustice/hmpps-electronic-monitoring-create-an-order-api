package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.AtLeastOneSelected
import java.time.LocalTime
import java.time.ZonedDateTime

@AtLeastOneSelected(
  fieldNames = ["curfew", "exclusionZone", "trail", "mandatoryAttendance", "alcohol"],
  message = "Select at least one monitoring type.",
)
data class UpdateMonitoringConditionsDto(
  @field:NotNull(message = "Order type is required")
  val orderType: String? = null,

  var curfew: Boolean? = null,

  @field:NotNull(message = "Monitoring conditions start date is required")
  var startDate: ZonedDateTime? = null,

  @field:NotNull(message = "Monitoring conditions start time is required")
  var startTime: LocalTime? = null,

  var endDate: ZonedDateTime? = null,

  var endTime: LocalTime? = null,

  var exclusionZone: Boolean? = null,

  var trail: Boolean? = null,

  var mandatoryAttendance: Boolean? = null,

  var alcohol: Boolean? = null,

  @field:NotNull(message = "Condition type is required")
  var conditionType: MonitoringConditionType? = null,

  val orderTypeDescription: OrderTypeDescription? = null,
) {
  @AssertTrue(message = "End date must be after start date")
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate!! > this.startDate
    }
    return true
  }

  @AssertTrue(message = "Please provide an end date and an end time, or leave both blank.")
  fun isEndTime(): Boolean {
    return (this.endDate == null) == (this.endTime == null)
  }
}
