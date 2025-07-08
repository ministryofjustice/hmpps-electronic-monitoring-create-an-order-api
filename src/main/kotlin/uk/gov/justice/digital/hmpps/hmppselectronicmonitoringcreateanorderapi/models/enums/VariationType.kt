package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class VariationType(val value: String) {
  CURFEW_HOURS("Change of curfew hours"),
  ADDRESS("Change of address"),
  ENFORCEMENT_ADD("Change to add an Inclusion or Exclusion Zone(s)"),
  ENFORCEMENT_UPDATE("Change to an existing Inclusion or Exclusion Zone(s)"),
  SUSPENSION("Order Suspension"),
  PERSONAL_DETAILS("Change to Personal Details"),
  ADD_AN_EXCLUSION_ZONES("Change to add an Inclusion or Exclusion Zone(s)"),
  AN_EXISTING_EXCLUSION("Change to an existing Inclusion or Exclusion"),
  CHANGE_TO_CURFEW_HOURS("Change to Curfew Hours"),
  ORDER_SUSPENSION("Order Suspension"),
  DEVICE_TYPE("Change to Device Type"),
  ENFORCEABLE_CONDITION("Change to Enforceable Condition"),
  ADMIN_ERROR("Admin Error "),
  OTHER("Other "),
  ;

}
