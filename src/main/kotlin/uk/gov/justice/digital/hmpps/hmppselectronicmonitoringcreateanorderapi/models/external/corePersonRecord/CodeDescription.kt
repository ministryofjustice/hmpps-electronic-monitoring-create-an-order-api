package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord

data class CodeDescription(val code: String?, val description: String?) {
  fun toSex(): String? = when (code) {
    "M" -> "MALE"
    "F" -> "FEMALE"
    "NS" -> "PREFER_NOT_TO_SAY"
    else -> "UNKNOWN"
  }
}
