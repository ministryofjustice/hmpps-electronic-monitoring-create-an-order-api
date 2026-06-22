package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

enum class UserRole(val code: String) {
  PRISON("ROLE_PRISON"),
  PROBATION("ROLE_PROBATION"),
  COURT("ROLE_EM_CEMO_COURT"),
  HOME_OFFICE("ROLE_EM_CEMO_HOME_OFFICE"),
}
