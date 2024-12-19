package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class OrderType(val value: String) {
  REQUEST("New Order"),
  VARIATION("Variation"),
}
