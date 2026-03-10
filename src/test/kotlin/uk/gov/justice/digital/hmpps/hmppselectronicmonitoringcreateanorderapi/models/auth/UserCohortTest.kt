package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserCohortTest {
  @Test
  fun `should return cohort tags for prison`() {
    val userCohort = UserCohort(Cohort.PRISON)
    val filter = userCohort.getTagFilter()

    assertThat(filter.tagGroups.size).isEqualTo(1)
    assertThat(filter.tagGroups[0].size).isEqualTo(1)
    assertThat(filter.tagGroups[0][0]).isEqualTo("PRISON")
  }

  @Test
  fun `should return cohort tags for probation`() {
    val userCohort = UserCohort(Cohort.PROBATION)
    val filter = userCohort.getTagFilter()

    assertThat(filter.tagGroups.size).isEqualTo(2)
    assertThat(filter.tagGroups[0][0]).isEqualTo("PRISON")
    assertThat(filter.tagGroups[1][0]).isEqualTo("Probation")
  }

  @Test
  fun `should return cohort tags for court`() {
    val userCohort = UserCohort(Cohort.COURT)
    val filter = userCohort.getTagFilter()

    assertThat(filter.tagGroups.size).isEqualTo(2)
    assertThat(filter.tagGroups[0][0]).isEqualTo("Civil Court")
    assertThat(filter.tagGroups[1][0]).isEqualTo("Family Court")
  }

  @Test
  fun `should return cohort tags for home office`() {
    val userCohort = UserCohort(Cohort.HOME_OFFICE)
    val filter = userCohort.getTagFilter()

    assertThat(filter.tagGroups.size).isEqualTo(1)
    assertThat(filter.tagGroups[0].size).isEqualTo(1)
    assertThat(filter.tagGroups[0][0]).isEqualTo("Home office")
  }

  @Test
  fun `should filter for only prison when caseLoadId matches standard prison`() {
    val userCohort = UserCohort(Cohort.PRISON, "Some AC name", "ACI")
    val filter = userCohort.getTagFilter()

    assertThat(filter.tagGroups.size).isEqualTo(1)
    assertThat(filter.tagGroups[0].size).isEqualTo(2)
    assertThat(filter.tagGroups[0][0]).isEqualTo("PRISON")
    assertThat(filter.tagGroups[0][1]).isEqualTo("ALTCOURSE_PRISON")
    assertThat(filter.noneOf.size).isEqualTo(2)
    assertThat(filter.noneOf[0]).isEqualTo("Youth YOI")
    assertThat(filter.noneOf[1]).isEqualTo("Youth YCS")
  }

//  TODO: implement test
//  @Test
//  fun `should filter for prison, YOI and YCS when caseLoadId matches YOI prison`() {
//    val userCohort = UserCohort(Cohort.PRISON, "Some AC name", "ACI")
//    val filter = userCohort.getTagFilter()
//
//    assertThat(filter.tagGroups.size).isEqualTo(2)
//    assertThat(filter.tagGroups[0]).isEqualTo("PRISON")
//    assertThat(filter.tagGroups[1]).isEqualTo("ALTCOURSE_PRISON")
//    assertThat(filter.noneOf.size).isEqualTo(2)
//    assertThat(filter.noneOf[0]).isEqualTo("Youth YOI")
//    assertThat(filter.noneOf[1]).isEqualTo("Youth YCS")
//  }

  // TODO:
  // Prison tags can be:
  // Prison,Name
  // Prison,Name,Youth YOI
  // Prison,Name,Youth YCS

  // Prison cohort, caseLoadId matches typical prison
  // Orders that have prison tag and matching prisonName tag, don't have Youth YOI or Youth YCS tag

  // Prison cohort, caseLoadId matches youth prison
  // Orders that have prison tag
}
