package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.time.DayOfWeek

data class UpdateCurfewTimetableDto(
  @Enumerated(EnumType.STRING)
  val dayOfWeek: DayOfWeek,

  @field:NotNull(message = ValidationErrors.CurfewTimetable.START_TIME_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewTimetable.START_TIME_REQUIRED)
  var startTime: String? = null,

  @field:NotNull(message = ValidationErrors.CurfewTimetable.END_TIME_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewTimetable.END_TIME_REQUIRED)
  var endTime: String? = null,

  @field:NotNull(message = ValidationErrors.CurfewTimetable.ADDRESS_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewTimetable.ADDRESS_REQUIRED)
  var curfewAddress: String? = null,
)
