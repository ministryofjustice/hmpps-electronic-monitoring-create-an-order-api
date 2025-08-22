package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class Pilot(val value: String) {
  ACQUISITIVE_CRIME_PROJECT("Acquisitive Crime Project"),
  DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_PROJECT("Domestic Abuse perpetrators on Licence Project"),
  LICENCE_VARIATION_PROJECT("Licence Variation Project"),
  DOMESTIC_ABUSE_PROTECTION_ORDER("Domestic Abuse Protection Order (DAPO)"),
  DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_DAPOL("Domestic Abuse Perpetrator on Licence (DAPOL)"),
  DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_HOME_DETENTION_CURFEW_DAPOL_HDC(
    "Domestic Abuse Perpetrator on Licence Home Detention Curfew (DAPOL HDC)",
  ),
  GPS_ACQUISITIVE_CRIME_HOME_DETENTION_CURFEW("GPS Acquisitive Crime Home Detention Curfew"),
  GPS_ACQUISITIVE_CRIME_PAROLE("GPS Acquisitive Crime Parole"),
  UNKNOWN(""),
}
