package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceType
import java.time.ZonedDateTime

data class UpdateTrailMonitoringConditionsDto(
  @field:NotNull(message = ValidationErrors.TrailMonitoringConditions.START_DATE_REQUIRED)
  val startDate: ZonedDateTime? = null,

  @field:NotNull(message = ValidationErrors.TrailMonitoringConditions.END_DATE_REQUIRED)
  @field:Future(message = ValidationErrors.TrailMonitoringConditions.END_DATE_MUST_BE_IN_FUTURE)
  val endDate: ZonedDateTime? = null,

  val deviceType: DeviceType? = null,
) {
  @AssertTrue(message = ValidationErrors.TrailMonitoringConditions.END_DATE_MUST_BE_AFTER_START_DATE)
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate > this.startDate
    }
    return true
  }
}
