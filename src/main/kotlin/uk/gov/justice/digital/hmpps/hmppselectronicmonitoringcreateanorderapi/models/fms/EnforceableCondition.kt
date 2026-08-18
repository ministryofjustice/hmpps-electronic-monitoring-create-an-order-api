package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class EnforceableCondition(
  val condition: String? = "",
  @JsonProperty("start_date")
  val startDate: String? = "",
  @JsonProperty("end_date")
  val endDate: String? = null,
) {
  @JsonIgnore
  fun isTrail(): Boolean = condition == "Location Monitoring (using Non-Fitted Device)" ||
    condition == "Location Monitoring (Fitted Device)"

  @JsonIgnore
  fun isAlcohol(): Boolean = condition == "AAMR" || condition == "AML"
}
