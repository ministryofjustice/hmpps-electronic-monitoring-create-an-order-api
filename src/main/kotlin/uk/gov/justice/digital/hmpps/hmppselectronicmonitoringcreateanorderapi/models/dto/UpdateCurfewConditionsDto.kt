package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.ZonedDateTime

data class UpdateCurfewConditionsDto(
  @field:NotNull(message = "Enter curfew start day")
  var startDate: ZonedDateTime? = null,

  @field:Future(message = "Curfew end day must be in the future")
  var endDate: ZonedDateTime? = null,

  @field:NotNull(message = "Curfew address is required")
  @field:Size(min = 1, message = "Curfew address is required")
  var curfewAddress: String? = null,
) {
  @AssertTrue(message = "End date must be after start date")
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate!! > this.startDate
    }
    return true
  }
}
