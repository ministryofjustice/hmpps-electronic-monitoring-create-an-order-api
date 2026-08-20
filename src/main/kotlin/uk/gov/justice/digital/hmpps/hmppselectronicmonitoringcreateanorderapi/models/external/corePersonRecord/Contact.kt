package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord

data class Contact(val type: CodeDescription?, val value: String?) {
  fun isMobile(): Boolean = type?.code == "MOBILE"

  fun isHome(): Boolean = type?.code == "HOME"
}
