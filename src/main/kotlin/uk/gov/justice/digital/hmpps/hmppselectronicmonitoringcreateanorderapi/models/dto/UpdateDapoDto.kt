package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.time.ZonedDateTime
import java.util.UUID

data class UpdateDapoDto(
  val id: UUID? = null,

  @field:Size(max = 20, message = ValidationErrors.Dapo.CAUSE_TOO_LONG)
  val clause: String? = null,

  val date: ZonedDateTime? = null,
)
