package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserCohortTest {
  @Test
  fun `should return cohort tags for prison`() {
    val userCohort = UserCohort(Cohort.PRISON)
    val tags = userCohort.cohortTags()

    assertThat(tags.size).isEqualTo(1)
    assertThat(tags[0]).isEqualTo("PRISON")
  }

  @Test
  fun `should return cohort tags for probation`() {
    val userCohort = UserCohort(Cohort.PROBATION)
    val tags = userCohort.cohortTags()

    assertThat(tags.size).isEqualTo(2)
    assertThat(tags.contains("PRISON")).isTrue
    assertThat(tags.contains("Probation")).isTrue
  }

  @Test
  fun `should return cohort tags for court`() {
    val userCohort = UserCohort(Cohort.COURT)
    val tags = userCohort.cohortTags()

    assertThat(tags.size).isEqualTo(1)
    assertThat(tags[0]).isEqualTo("Court")
  }

  @Test
  fun `should return cohort tags for home office`() {
    val userCohort = UserCohort(Cohort.HOME_OFFICE)
    val tags = userCohort.cohortTags()

    assertThat(tags.size).isEqualTo(1)
    assertThat(tags[0]).isEqualTo("Home office")
  }

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
