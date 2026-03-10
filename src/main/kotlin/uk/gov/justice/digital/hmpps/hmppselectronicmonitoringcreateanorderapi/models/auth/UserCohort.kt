package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

data class UserCohort(val cohort: Cohort, val activeCaseLoadName: String? = "", val activeCaseLoadId: String? = "") {
  fun cohortTags(): List<String> = when (this.cohort) {
    Cohort.PRISON -> listOf("PRISON")
    Cohort.PROBATION -> listOf("PRISON", "Probation")
    Cohort.COURT -> listOf("Court")
    Cohort.HOME_OFFICE -> listOf("Home office")
    Cohort.OTHER -> listOf()
  }
}
