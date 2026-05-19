package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class CaseState(val value: String) {
  NEW("1"),
  CLOSED("3"),
  RESOLVED("6"),
  CANCELLED("7"),
  OPEN("10"),
  AWAITING_INFO("18"),
  AWAITING_VALIDATION("11"),
  UNKNOWN("Unknown"),
  ;

  companion object {
    fun fromStateString(value: String?): CaseState = CaseState.entries.firstOrNull { it.value == value } ?: UNKNOWN
  }
}
