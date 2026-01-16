package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.ManageUserApiExtension
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.ManageUserApiExtension.Companion.manageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.CaseLoad
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohorts
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCaseLoad
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort

@ExtendWith(

  ManageUserApiExtension::class,
)
class UserCohortControllerTest : IntegrationTestBase() {
  @BeforeEach
  fun setup() {
    val mockUserCohort = UserCaseLoad(
      "mockUser",
      true,
      "mock account",
      CaseLoad("ABC", "HMP ABC"),
      emptyList<CaseLoad>(),
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

    Assertions.assertThat(result.responseBody?.cohort).isEqualTo(Cohorts.PRISON)
    Assertions.assertThat(result.responseBody?.activeCaseLoad).isEqualTo("HMP ABC")
  }
}
