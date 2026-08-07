package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import com.fasterxml.jackson.annotation.JsonProperty

data class Up3EnforceableCondition(
  var condition: String,
  @JsonProperty("start_date")
  var startDate: String,
  @JsonProperty("end_date")
  var endDate: String,
) {
  fun isTrail(): Boolean = condition == "Location Monitoring (using Non-Fitted Device)" ||
    condition == "Location Monitoring (Fitted Device)"

  fun isAlcohol(): Boolean = condition == "AAMR" || condition == "AML"
}
