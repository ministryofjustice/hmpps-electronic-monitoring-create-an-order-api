package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SentenceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.AtLeastOneSelected
import java.time.ZonedDateTime

@AtLeastOneSelected(
  fieldNames = ["curfew", "exclusionZone", "trail", "mandatoryAttendance", "alcohol"],
  message = "Select at least one monitoring type.",
)
data class UpdateMonitoringConditionsDto(
  @field:NotNull(message = "Order type is required")
  val orderType: OrderType? = null,

  var curfew: Boolean? = null,

  @field:NotNull(message = "Monitoring conditions start date is required")
  val startDate: ZonedDateTime? = null,

  val endDate: ZonedDateTime? = null,

  val exclusionZone: Boolean? = null,

  val trail: Boolean? = null,

  val mandatoryAttendance: Boolean? = null,

  val alcohol: Boolean? = null,

  @field:NotNull(message = "Condition type is required")
  val conditionType: MonitoringConditionType? = null,

  val orderTypeDescription: OrderTypeDescription? = null,

  val sentenceType: SentenceType? = null,

  val issp: YesNoUnknown = YesNoUnknown.UNKNOWN,

  val hdc: YesNoUnknown = YesNoUnknown.UNKNOWN,

  val prarr: YesNoUnknown = YesNoUnknown.UNKNOWN,
) {
  @AssertTrue(message = "End date must be after start date")
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate > this.startDate
    }
    return true
  }
}
