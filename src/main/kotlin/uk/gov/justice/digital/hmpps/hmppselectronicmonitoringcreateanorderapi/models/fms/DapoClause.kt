package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonProperty

data class DapoClause(
  @JsonProperty("dapo_order_clause_number")
  val dapoOrderClauseNumber: String? = "",
  val date: String? = "",
)
