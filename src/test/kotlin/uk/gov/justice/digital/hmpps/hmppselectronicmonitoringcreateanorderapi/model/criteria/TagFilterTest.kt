package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.criteria

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.TagFilter

class TagFilterTest {
  @Test
  fun `should return cohort tags for probation`() {
    val userCohort = UserCohort(Cohort.PROBATION)
    val filter = TagFilter.getTagFilterByUserCohort(userCohort)

    val expected = TagFilter().anyOf("PRISON", "Probation")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should return cohort tags for court`() {
    val userCohort = UserCohort(Cohort.COURT)
    val filter = TagFilter.getTagFilterByUserCohort(userCohort)

    val expected = TagFilter().anyOf("Civil Court", "Family Court")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should return cohort tags for home office`() {
    val userCohort = UserCohort(Cohort.HOME_OFFICE)
    val filter = TagFilter.getTagFilterByUserCohort(userCohort)

    val expected = TagFilter().anyOf("Home office")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should filter for only prison when caseLoadId matches standard prison`() {
    val userCohort = UserCohort(Cohort.PRISON, "Some AC name", "ACI")
    val filter = TagFilter.getTagFilterByUserCohort(userCohort)

    val expected = TagFilter().allOf("PRISON", "ALTCOURSE_PRISON").exclude("Youth YOI", "Youth YCS")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should filter for prison, YOI and YCS when caseLoadId matches YOI prison`() {
    val userCohort = UserCohort(Cohort.PRISON, "Some AC name", "AGI")
    val filter = TagFilter.getTagFilterByUserCohort(userCohort)

    val expected = TagFilter().allOf("PRISON", "ASKHAM_GRANGE_PRISON_AND_YOUNG_OFFENDER_INSTITUTION")
      .anyOf("Youth YCS")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `should filter for prison, YOI, and YCS when no prison matches`() {
    val userCohort = UserCohort(Cohort.PRISON)
    val filter = TagFilter.getTagFilterByUserCohort(userCohort)

    val expected = TagFilter().anyOf("Youth YCS")

    assertThat(filter).isEqualTo(expected)
  }

  @Test
  fun `true if match single tag`() {
    val tags = "TAG ONE"
    val filter = TagFilter().allOf("TAG ONE")

    assertThat(filter.matchesTags(tags)).isTrue
  }

  @Test
  fun `false is no match no tag`() {
    val tags = "TAG TWO"
    val filter = TagFilter().anyOf("TAG ONE")

    assertThat(filter.matchesTags(tags)).isFalse
  }

  @Test
  fun `should match any of multiple tags`() {
    val tags = "TAG TWO"
    val filter = TagFilter().anyOf("TAG ONE", "TAG TWO")

    assertThat(filter.matchesTags(tags)).isTrue
  }

  @Test
  fun `true if does match all of multiple tags`() {
    val tags = "TAG ONE,TAG TWO"
    val filter = TagFilter().allOf("TAG ONE", "TAG TWO")

    assertThat(filter.matchesTags(tags)).isTrue
  }

  @Test
  fun `false if doesn't match all of multiple tags`() {
    val tags = "TAG ONE"
    val filter = TagFilter().allOf("TAG ONE", "TAG TWO")

    assertThat(filter.matchesTags(tags)).isFalse
  }

  @Test
  fun `false if includes an excluded tag`() {
    val tags = "TAG ONE,TAG TWO"
    val filter = TagFilter().anyOf("TAG ONE").exclude("TAG TWO")

    assertThat(filter.matchesTags(tags)).isFalse
  }

  @Test
  fun `should not match partial tags`() {
    val tags = "TAG ONE,TAG TWO"
    val filter = TagFilter().anyOf("TAG")

    assertThat(filter.matchesTags(tags)).isFalse
  }

  @Test
  fun `matches independent of case`() {
    val tags = "TAG ONE,TAG TWO"
    val filter = TagFilter().anyOf("tag one")

    assertThat(filter.matchesTags(tags)).isTrue
  }

  @Test
  fun `returns true if tags are empty`() {
    val tags = ""
    val filter = TagFilter().anyOf("tag one")

    assertThat(filter.matchesTags(tags)).isTrue
  }

  @Test
  fun `returns true if filter is empty`() {
    val tags = "TAG ONE"
    val filter = TagFilter()

    assertThat(filter.matchesTags(tags)).isTrue
  }
}
