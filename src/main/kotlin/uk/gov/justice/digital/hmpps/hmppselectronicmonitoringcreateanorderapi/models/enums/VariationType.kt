package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class VariationType(val value: String) {
  CURFEW_HOURS("Change of curfew hours"),
  ADDRESS("Change of address"),
  ENFORCEMENT_ADD("Change to add an Inclusion or Exclusion Zone(s)"),
  ENFORCEMENT_UPDATE("Change to an existing Inclusion or Exclusion Zone(s)"),
  SUSPENSION("Order Suspension"),
}
