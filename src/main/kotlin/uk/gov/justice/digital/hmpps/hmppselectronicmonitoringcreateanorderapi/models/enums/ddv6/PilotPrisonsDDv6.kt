package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6

enum class PilotPrisonsDDv6(val value: String) {
  CARDIFF_PRISON("Cardiff Prison"),
  FOSSE_WAY_PRISON("Fosse Way Prison"),
  PETERBOROUGH_PRISON("Peterborough Prison"),
  RANBY_PRISON("Ranby Prison"),
  SUDBURY_PRISON("Sudbury Prison"),
  SWANSEA_PRISON("Swansea Prison"),
  ;

  companion object {
    fun from(value: String?): PilotPrisonsDDv6? = entries.firstOrNull {
      it.name == value
    }
  }
}
