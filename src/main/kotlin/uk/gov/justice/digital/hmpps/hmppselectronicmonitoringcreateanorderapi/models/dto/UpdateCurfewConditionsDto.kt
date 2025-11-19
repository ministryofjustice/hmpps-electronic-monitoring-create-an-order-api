package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.time.ZonedDateTime

data class UpdateCurfewConditionsDto(
  @field:NotNull(message = ValidationErrors.CurfewConditions.START_DATE_REQUIRED)
  var startDate: ZonedDateTime? = null,

  @field:NotNull(message = ValidationErrors.CurfewConditions.END_DATE_REQUIRED)
  @field:Future(message = ValidationErrors.CurfewConditions.END_DATE_MUST_BE_IN_FUTURE)
  var endDate: ZonedDateTime? = null,

  var curfewAddress: String? = null,
) {
  @AssertTrue(message = ValidationErrors.CurfewConditions.END_DATE_MUST_BE_AFTER_START_DATE)
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate!! > this.startDate
    }
    return true
  }
}
