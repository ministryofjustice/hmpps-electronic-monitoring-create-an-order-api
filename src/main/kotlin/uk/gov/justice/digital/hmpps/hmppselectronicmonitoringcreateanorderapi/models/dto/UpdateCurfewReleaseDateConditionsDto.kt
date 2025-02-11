package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.time.ZonedDateTime

data class UpdateCurfewReleaseDateConditionsDto(
  @field:NotNull(message = "Enter curfew release date")
  var releaseDate: ZonedDateTime? = null,

  @field:NotNull(message = "Enter start time")
  @field:Size(min = 1, message = "Enter start time")
  var startTime: String? = null,

  @field:NotNull(message = "Enter end time")
  @field:Size(min = 1, message = "Enter end time")
  var endTime: String? = null,

  @Enumerated(EnumType.STRING)
  @field:NotNull(message = "Curfew address is required")
  var curfewAddress: AddressType? = null,
)
