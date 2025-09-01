package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.enums.ddv5

enum class MilitaryCourtDDv5(val value: String) {
  BULFORD_MILITARY_COURT_CENTRE("Bulford Military Court Centre"),
  CATTERICK_MILITARY_COURT_CENTRE("Catterick Military Court Centre"),
  ;

  companion object {
    fun from(value: String?): MilitaryCourtDDv5? = entries.firstOrNull {
      it.name == value
    }
  }
}
