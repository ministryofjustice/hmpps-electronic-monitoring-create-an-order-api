package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors

data class UpdateNoFixedAbodeDto(
  @field:NotNull(message = ValidationErrors.NoFixedAbode.NO_FIXED_ABODE_REQUIRED)
  var noFixedAbode: Boolean? = null,
)
