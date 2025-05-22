package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.time.ZonedDateTime

data class UpdateTrailMonitoringConditionsDto(
  @field:NotNull(message = ValidationErrors.TrailMonitoringConditions.START_DATE_REQUIRED)
  val startDate: ZonedDateTime? = null,
  @field:NotNull(message = ValidationErrors.TrailMonitoringConditions.END_DATE_REQUIRED)
  @field:Future(message = ValidationErrors.TrailMonitoringConditions.END_DATE_MUST_BE_IN_FUTURE)
  val endDate: ZonedDateTime? = null,
)
