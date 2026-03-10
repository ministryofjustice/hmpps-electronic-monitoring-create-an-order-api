package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

data class UserCohort(val cohort: Cohort, val activeCaseLoadName: String? = "", val activeCaseLoadId: String? = "") {
  // TODO: Update with correct tags based on activeCaseLoadId
  fun accessibleTags(): List<String> = when (this.cohort) {
    Cohort.PRISON -> listOf("PRISON")
    Cohort.PROBATION -> listOf("PRISON", "PROBATION")
    Cohort.COURT -> listOf("COURT")
    Cohort.HOME_OFFICE -> listOf("HOME_OFFICE")
    Cohort.OTHER -> listOf()
  }
}
