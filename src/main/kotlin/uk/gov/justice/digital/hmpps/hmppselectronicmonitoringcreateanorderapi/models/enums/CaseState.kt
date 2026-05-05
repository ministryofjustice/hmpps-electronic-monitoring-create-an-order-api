package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class CaseState(val value: String) {
  NEW("New"),
  CLOSED("Closed"),
  RESOLVED("Resolved"),
  CANCELLED("Cancelled"),
  OPEN("Open"),
  AWAITING_INFO("AwaitingInfo"),
  UNKNOWN("Unknown"),
  ;

  companion object {
    fun fromStateString(value: String?): CaseState = when (value) {
      "1" -> NEW
      "3" -> CLOSED
      "6" -> RESOLVED
      "7" -> CANCELLED
      "10" -> OPEN
      "18" -> AWAITING_INFO
      else -> UNKNOWN
    }
  }
}
