package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime

data class UpdateTrailMonitoringConditionsDto(
  @field:NotNull(message = "Start date is required")
  @field:Future(message = "Start date must be in the future")
  val startDate: ZonedDateTime? = null,
  @field:Future(message = "End date must be in the future")
  val endDate: ZonedDateTime? = null,
)
