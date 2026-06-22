package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.auth

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserGroup
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.toCohort

class UserGroupTest {

  @Test
  fun `isCourtGroup returns true when groupCode is CEMO_CRT_USERS`() {
    val group = UserGroup(groupName = "group name", groupCode = "CEMO_CRT_USERS")

    assert(group.isCourtGroup())
  }

  @Test
  fun `isHomeOfficeGroup returns true when groupCode is not CEMO_HO_USERS`() {
    val group = UserGroup(groupName = "group name", groupCode = "CEMO_HO_USERS")

    assert(group.isHomeOfficeGroup())
  }

  @Test
  fun `group list returns court cohort if court in list`() {
    val groups = listOf(UserGroup(groupName = "group name", groupCode = "CEMO_CRT_USERS"))

    assert(groups.toCohort() == Cohort.COURT)
  }

  @Test
  fun `group list returns home office cohort if home office in list`() {
    val groups = listOf(UserGroup(groupName = "group name", groupCode = "CEMO_HO_USERS"))

    assert(groups.toCohort() == Cohort.HOME_OFFICE)
  }

  @Test
  fun `group list returns other cohort in neither in list`() {
    val groups = listOf(UserGroup(groupName = "group name", groupCode = "OTHER_CODE"))

    assert(groups.toCohort() == Cohort.OTHER)
  }
}
