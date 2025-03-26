package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components

import com.fasterxml.jackson.annotation.JsonProperty

data class EnforceableCondition(
  val condition: String? = "",

  @JsonProperty("start_date")
  val startDate: String? = "",

  @JsonProperty("end_date")
  val endDate: String? = null,
)
