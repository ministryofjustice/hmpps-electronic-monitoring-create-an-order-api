package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.AtLeastOneSelected
import java.time.ZonedDateTime

@AtLeastOneSelected(
  fieldNames = ["curfew", "exclusionZone", "trail", "mandatoryAttendance", "alcohol"],
  message = "Select at least one monitoring type.",
)
data class UpdateMonitoringConditionsDto(
  @field:NotNull(message = "Order type is required")
  val orderType: String? = null,

  val devicesRequired: Array<String>? = null,

  var acquisitiveCrime: Boolean? = null,

  var dapol: Boolean? = null,

  var curfew: Boolean? = null,

  @field:NotNull(message = "Monitoring conditions start date is required")
  @field:Future(message = "Monitoring conditions start date must be in the future")
  var startDate: ZonedDateTime? = null,

  var endDate: ZonedDateTime? = null,

  var exclusionZone: Boolean? = null,

  var trail: Boolean? = null,

  var mandatoryAttendance: Boolean? = null,

  var alcohol: Boolean? = null,

  @field:NotNull(message = "Condition type is required")
  var conditionType: MonitoringConditionType? = null,

  @field:NotNull(message = "Order type description type is required")
  val orderTypeDescription: OrderTypeDescription? = null,
) {
  @AssertTrue(message = "End date must be after start date")
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate!! > this.startDate
    }
    return true
  }
}