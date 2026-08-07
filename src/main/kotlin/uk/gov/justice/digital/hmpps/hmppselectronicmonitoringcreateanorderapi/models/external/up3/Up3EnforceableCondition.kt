package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

data class Up3EnforceableCondition(var condition: String, var startDate: String, var endDate: String) {
  fun isTrail(): Boolean = condition == "Location Monitoring (using Non-Fitted Device)" ||
    condition == "Location Monitoring (Fitted Device)"

  fun isAlcohol(): Boolean = condition == "AAMR" || condition == "AML"
}
