package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.time.ZonedDateTime

data class UpdateCurfewReleaseDateConditionsDto(
  var releaseDate: ZonedDateTime? = null,

  @field:NotNull(message = ValidationErrors.CurfewReleaseDateConditions.START_TIME_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewReleaseDateConditions.START_TIME_REQUIRED)
  var startTime: String? = null,

  @field:NotNull(message = ValidationErrors.CurfewReleaseDateConditions.END_TIME_REQUIRED)
  @field:Size(min = 1, message = ValidationErrors.CurfewReleaseDateConditions.END_TIME_REQUIRED)
  var endTime: String? = null,

  @Enumerated(EnumType.STRING)
  @field:NotNull(message = ValidationErrors.CurfewReleaseDateConditions.ADDRESS_REQUIRED)
  var curfewAddress: AddressType? = null,
)
