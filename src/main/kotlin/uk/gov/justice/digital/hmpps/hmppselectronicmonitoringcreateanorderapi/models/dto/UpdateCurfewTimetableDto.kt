package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.DayOfWeek

data class UpdateCurfewTimetableDto(
  @Enumerated(EnumType.STRING)
  val dayOfWeek: DayOfWeek,

  @field:NotNull(message = "Enter start time of curfew")
  @field:Size(min = 1, message = "Enter start time of curfew")
  var startTime: String? = null,

  @field:NotNull(message = "Enter end time of curfew")
  @field:Size(min = 1, message = "Enter end time of curfew")
  var endTime: String? = null,

  @field:NotNull(message = "Curfew address is required")
  @field:Size(min = 1, message = "Curfew address is required")
  var curfewAddress: String? = null,
)
