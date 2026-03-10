package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.TagFilter
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison

data class UserCohort(val cohort: Cohort, val activeCaseLoadName: String? = "", val activeCaseLoadId: String? = "") {
  fun getTagFilter(): TagFilter = when (this.cohort) {
    Cohort.PRISON -> {
      val prisonName = Prison.fromId(activeCaseLoadId)?.name
      val allOfTags = mutableListOf("PRISON")
      if (!prisonName.isNullOrEmpty()) {
        allOfTags.add(prisonName)
      }

      return TagFilter(tagGroups = listOf(allOfTags), noneOf = listOf("Youth YOI", "Youth YCS"))
    }

    Cohort.PROBATION -> TagFilter(tagGroups = listOf(listOf("PRISON"), listOf("Probation")))
    Cohort.COURT -> TagFilter(tagGroups = listOf(listOf("Civil Court"), listOf("Family Court")))
    Cohort.HOME_OFFICE -> TagFilter(tagGroups = listOf(listOf("Home office")))
    Cohort.OTHER -> TagFilter()
  }
}
