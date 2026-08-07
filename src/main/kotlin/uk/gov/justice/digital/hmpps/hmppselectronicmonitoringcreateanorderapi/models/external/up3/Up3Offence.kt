package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import com.fasterxml.jackson.annotation.JsonProperty

data class Up3Offence(
  var offence: String,
  @JsonProperty("offence_date")
  var offenceDate: String,
)
