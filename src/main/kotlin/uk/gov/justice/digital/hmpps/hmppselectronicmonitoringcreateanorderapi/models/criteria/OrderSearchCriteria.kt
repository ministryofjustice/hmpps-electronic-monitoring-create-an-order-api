package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison

data class OrderSearchCriteria(val searchTerm: String = "", val tagFilter: TagFilter = TagFilter())

data class TagFilter(val tagGroups: List<List<String>> = emptyList(), val exclude: List<String> = emptyList()) {
  fun allOf(vararg tags: String): TagFilter = copy(tagGroups = tagGroups + listOf(tags.toList()))

  fun anyOf(vararg tags: String): TagFilter = copy(tagGroups = tagGroups + tags.map { listOf(it) })

  fun exclude(vararg tags: String): TagFilter = copy(exclude = exclude + tags.toList())

  fun matchesTags(tags: String?): Boolean {
    if (tags.isNullOrEmpty() || (tagGroups.isEmpty() && exclude.isEmpty())) {
      return true
    }

    val tagList = tags.split(",").map { it.lowercase() }

    if (exclude.any { tagList.contains(it.lowercase()) }) {
      return false
    }
    return tagGroups.any { group -> group.all { tagList.contains(it.lowercase()) } }
  }

  companion object {
    fun getTagFilterByUserCohort(userCohort: UserCohort): TagFilter = when (userCohort.cohort) {
      Cohort.PRISON -> {
        // admin ID used in dev, do not filter orders
        if (userCohort.activeCaseLoadId == "CADM_I") {
          return TagFilter()
        }

        val prisons = Prison.fromId(userCohort.activeCaseLoadId)

        if (prisons.isEmpty()) {
          return TagFilter().allOf("Youth YCS")
        }

        var filter = TagFilter()

        prisons.forEach { prison ->
          filter = filter.allOf("PRISON", prison.name)
        }

        if (prisons.any { prison -> Prison.isPrisonYOI(prison) }) {
          // Prison is YOI, return matching prison and Youth YCS
          filter = filter.allOf("Youth YCS")
        } else {
          // Prison is adult, return matching prison, exclude Youth YOI and Youth YCS
          filter = filter.exclude("Youth YOI", "Youth YCS")
        }

        return filter
      }

      Cohort.PROBATION -> TagFilter().anyOf("PRISON", "Probation")
      Cohort.COURT -> TagFilter().anyOf("Civil Court", "Family Court")
      Cohort.HOME_OFFICE -> TagFilter().anyOf("Home office")
      Cohort.OTHER -> TagFilter()
    }
  }
}
