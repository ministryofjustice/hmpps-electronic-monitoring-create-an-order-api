package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.TagFilter
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison

data class UserCohort(val cohort: Cohort, val activeCaseLoadName: String? = "", val activeCaseLoadId: String? = "") {
  fun getTagFilter(): TagFilter = when (this.cohort) {
    Cohort.PRISON -> {
      val prison = Prison.fromId(activeCaseLoadId) ?: return TagFilter().allOf("Youth YCS")

      val filter = TagFilter().allOf("PRISON", prison.name)
      if (Prison.isPrisonYOI(prison)) {
        // Prison is YOI, return matching prison and Youth YCS
        return filter.allOf("Youth YCS")
      }

      // Prison is adult, return matching prison, exclude Youth YOI and Youth YCS
      return filter.exclude("Youth YOI", "Youth YCS")
    }

    Cohort.PROBATION -> TagFilter().anyOf("PRISON", "Probation")
    Cohort.COURT -> TagFilter().anyOf("Civil Court", "Family Court")
    Cohort.HOME_OFFICE -> TagFilter().anyOf("Home office")
    Cohort.OTHER -> TagFilter()
  }
}
