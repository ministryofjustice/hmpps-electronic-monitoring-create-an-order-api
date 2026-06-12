package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class VariationType(val value: String, val priority: Int = Int.MAX_VALUE) {
  CHANGE_TO_ADDRESS("Change to Address", 1),
  CHANGE_TO_ADD_AN_EXCLUSION_ZONES("Change to add an Inclusion or Exclusion Zone(s)", 2),
  CHANGE_TO_AN_EXISTING_EXCLUSION("Change to an existing Inclusion or Exclusion Zone(s)", 3),
  CHANGE_TO_CURFEW_HOURS("Change to Curfew Hours", 4),
  CHANGE_TO_DEVICE_TYPE("Change to Device Type", 5),
  CHANGE_TO_ENFORCEABLE_CONDITION("Change to Enforceable Condition", 6),
  CHANGE_TO_PERSONAL_DETAILS("Change to Personal Details", 7),
  OTHER("Other", 8),

  // Legacy
  ADDRESS("Change of address"),
  ENFORCEMENT_ADD("Change to add an Inclusion or Exclusion Zone(s)"),
  ENFORCEMENT_UPDATE("Change to an existing Inclusion or Exclusion Zone(s)"),
  CURFEW_HOURS("Change of curfew hours"),
  ORDER_SUSPENSION("Order Suspension"),
  SUSPENSION("Order Suspension"),
  ADMIN_ERROR("Admin Error"),
  ;

  companion object {
    val DDv4_TYPES = listOf(
      CURFEW_HOURS,
      ADDRESS,
      ENFORCEMENT_ADD,
      ENFORCEMENT_UPDATE,
      SUSPENSION,
    )

    val DDv5_TYPES = listOf(
      CHANGE_TO_ADDRESS,
      CHANGE_TO_PERSONAL_DETAILS,
      CHANGE_TO_ADD_AN_EXCLUSION_ZONES,
      CHANGE_TO_AN_EXISTING_EXCLUSION,
      CHANGE_TO_CURFEW_HOURS,
      ORDER_SUSPENSION,
      CHANGE_TO_DEVICE_TYPE,
      CHANGE_TO_ENFORCEABLE_CONDITION,
      ADMIN_ERROR,
      OTHER,
    )

    fun from(value: String?): VariationType? = VariationType.entries.firstOrNull {
      it.name == value
    }
  }
}
