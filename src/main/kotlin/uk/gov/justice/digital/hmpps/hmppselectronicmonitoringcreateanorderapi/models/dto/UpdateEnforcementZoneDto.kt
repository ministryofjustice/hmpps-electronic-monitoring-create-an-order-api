package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import java.time.ZonedDateTime

data class UpdateEnforcementZoneDto(
  @field:NotNull(message = "Enforcement zone description is required")
  @field:Size(min = 1, message = "Enforcement zone description is required")
  var description: String? = null,

  @field:NotNull(message = "Enforcement zone duration is required")
  @field:Size(min = 1, message = "Enforcement zone duration is required")
  var duration: String? = null,

  @field:Future(message = "Enforcement zone end date must be in the future")
  var endDate: ZonedDateTime? = null,

  @field:NotNull(message = "Enforcement zone start date is required")
  var startDate: ZonedDateTime? = null,

  var zoneId: Int? = null,

  @Enumerated(EnumType.STRING)
  @field:NotNull(message = "Enforcement zone type is required")
  var zoneType: EnforcementZoneType? = null,
)
