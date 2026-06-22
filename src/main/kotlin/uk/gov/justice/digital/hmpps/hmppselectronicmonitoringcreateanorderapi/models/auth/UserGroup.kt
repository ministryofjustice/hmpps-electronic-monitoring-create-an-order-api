package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

data class UserGroup(val groupCode: String, val groupName: String) {
  fun isCourtGroup(): Boolean = groupCode == "CEMO_CRT_USERS"

  fun isHomeOfficeGroup(): Boolean = groupCode == "CEMO_HO_USERS"
}

fun List<UserGroup>.toCohort(): Cohort = when {
  any { it.isCourtGroup() } -> Cohort.COURT
  any { it.isHomeOfficeGroup() } -> Cohort.HOME_OFFICE
  else -> Cohort.OTHER
}
