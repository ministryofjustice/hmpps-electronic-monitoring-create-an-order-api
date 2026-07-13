package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class DataDictionaryVersion {
  DDV4,
  DDV5,
  DDV6,
  DDV7,
  ;

  fun isLaterThanOrEqual(compareVersion: DataDictionaryVersion): Boolean = this >= compareVersion
}
