package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import com.fasterxml.jackson.annotation.JsonProperty

data class Up3Dapo(
  @JsonProperty("dapo_order_clause_number")
  var dapoOrderClauseNumber: String,
  var date: String,
)
