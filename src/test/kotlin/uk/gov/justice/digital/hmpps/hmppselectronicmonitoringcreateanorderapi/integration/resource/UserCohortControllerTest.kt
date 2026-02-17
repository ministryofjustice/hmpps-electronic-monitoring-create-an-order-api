package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.ManageUserApiExtension
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.ManageUserApiExtension.Companion.manageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserGroup
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsCaseload
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsUserCaseloadResponse

@ExtendWith(

  ManageUserApiExtension::class,
)
class UserCohortControllerTest : IntegrationTestBase() {
  @BeforeEach
  fun setup() {
    val mockUserCohort = HmppsUserCaseloadResponse(
      "mockUser",
      true,
      "mock account",
      HmppsCaseload("ABC", "HMP ABC"),
      emptyList<HmppsCaseload>(),
    )
    manageUserApi.stubUserActiveCaseLoad(mockUserCohort)
  }

  @Test
  fun `Should return user cohort`() {
    val result = webTestClient.get().uri("/api/user-cohort")
      .headers(setAuthorisation("AUTH_ADM", roles = listOf("ROLE_EM_CEMO__CREATE_ORDER", "ROLE_PRISON")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(UserCohort::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody?.cohort).isEqualTo(Cohort.PRISON)
    Assertions.assertThat(result.responseBody?.activeCaseLoad).isEqualTo("HMP ABC")
  }

  @Test
  fun `Should return user cohort via groups`() {
    val mockUserGroups =
      listOf(UserGroup(groupName = "Create an Electronic Monitoring Order Court Users", groupCode = "CEMO_CRT_USERS"))

    manageUserApi.stubGetUserGroups(mockUserGroups)

    val result = webTestClient.get().uri("/api/user-cohort")
      .headers(setAuthorisation("AUTH_ADM", roles = listOf("ROLE_EM_CEMO__CREATE_ORDER", "ROLE_OTHER")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(UserCohort::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody?.cohort).isEqualTo(Cohort.COURT)
  }
}
