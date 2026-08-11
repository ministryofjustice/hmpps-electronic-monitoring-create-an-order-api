package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonProperty

data class OffenceData(
  val offence: String? = "",
  @JsonProperty("offence_date")
  val offenceDate: String? = "",
)
