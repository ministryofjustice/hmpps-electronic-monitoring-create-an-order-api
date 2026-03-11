package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.TagFilter

class UserCohortTest {
  @Test
  fun `should return cohort tags for probation`() {
    val userCohort = UserCohort(Cohort.PROBATION)
    val filter = userCohort.getTagFilter()

    val expected = TagFilter().anyOf("PRISON", "Probation")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should return cohort tags for court`() {
    val userCohort = UserCohort(Cohort.COURT)
    val filter = userCohort.getTagFilter()

    val expected = TagFilter().anyOf("Civil Court", "Family Court")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should return cohort tags for home office`() {
    val userCohort = UserCohort(Cohort.HOME_OFFICE)
    val filter = userCohort.getTagFilter()

    val expected = TagFilter().anyOf("Home office")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should filter for only prison when caseLoadId matches standard prison`() {
    val userCohort = UserCohort(Cohort.PRISON, "Some AC name", "ACI")
    val filter = userCohort.getTagFilter()

    val expected = TagFilter().allOf("PRISON", "ALTCOURSE_PRISON").exclude("Youth YOI", "Youth YCS")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should filter for prison, YOI and YCS when caseLoadId matches YOI prison`() {
    val userCohort = UserCohort(Cohort.PRISON, "Some AC name", "AGI")
    val filter = userCohort.getTagFilter()

    val expected = TagFilter().allOf("PRISON", "ASKHAM_GRANGE_PRISON_AND_YOUNG_OFFENDER_INSTITUTION")
      .anyOf("Youth YCS")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should filter for prison, YOI, and YCS when no prison matches`() {
    val userCohort = UserCohort(Cohort.PRISON)
    val filter = userCohort.getTagFilter()

    val expected = TagFilter().anyOf("Youth YCS")

    assertThat(filter).isEqualTo(expected)
  }
}
