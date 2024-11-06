package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull

data class UpdateNoFixedAbodeDto(
  @field:NotNull(message = "You must indicate whether the device wearer has a fixed abode")
  var noFixedAbode: Boolean? = null,
)
