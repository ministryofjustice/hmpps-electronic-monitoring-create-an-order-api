package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class OrderTypeDescription(val value: String?) {
  DAPO("DAPO"),
  DAPOL("DAPOL"),
  DAPOL_HDC("DAPOL HDC"),
  GPS_ACQUISITIVE_CRIME_HDC("GPS Acquisitive Crime HDC"),
  GPS_ACQUISITIVE_CRIME_PAROLE("GPS Acquisitive Crime Parole"),
  UNKNOWN(null),
}
