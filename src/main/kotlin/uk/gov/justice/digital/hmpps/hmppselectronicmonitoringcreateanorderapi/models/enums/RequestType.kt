package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class RequestType(val value: String) {
  REQUEST("New Order"),
  VARIATION("Variation"),
  REJECTED("Rejected"),
  AMEND_ORIGINAL_REQUEST("New Order"),
}
