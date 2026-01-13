package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.ManageUserApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.CaseLoad
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohorts
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCaseLoad

@ActiveProfiles("test")
@JsonTest
class UserCohortServiceTest {

  private lateinit var authentication: JwtAuthenticationToken
  private lateinit var jwtToken: Jwt
  private lateinit var client: ManageUserApiClient
  private lateinit var service: UserCohortService

  @BeforeEach
  fun setup() {
    authentication = mock(JwtAuthenticationToken::class.java)
    jwtToken = mock(Jwt::class.java)
    `when`(authentication.token).thenReturn(jwtToken)

    client = mock(ManageUserApiClient::class.java)
    service = UserCohortService(client)
  }

  @Test
  fun `Should return cohort of prison when user has role ROLE_PRISON`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PRISON" }))

    val reuslt = service.getUserCohort(authentication)

    Assertions.assertThat(reuslt.cohort).isEqualTo(Cohorts.PRISON)
  }

  @Test
  fun `Should return active caseload for prison users`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PRISON" }))
    `when`(jwtToken.tokenValue).thenReturn("Mock Token")
    `when`(client.getUserActiveCaseload(authentication.token)).thenReturn(
      UserCaseLoad(
        "mockUser",
        true,
        "mock account",
        CaseLoad("ABC", "HMP ABC"),
        emptyList<CaseLoad>(),
      ),
    )
    val result = service.getUserCohort(authentication)

    Assertions.assertThat(result.cohort).isEqualTo(Cohorts.PRISON)
    Assertions.assertThat(result.activeCaseLoad).isEqualTo("HMP ABC")
  }

  @Test
  fun `Should return cohort of probation when user has role ROLE_PROBATION`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PROBATION" }))

    val reuslt = service.getUserCohort(authentication)

    Assertions.assertThat(reuslt.cohort).isEqualTo(Cohorts.PROBATION)
  }

  @Test
  fun `Should return cohort of courts when user has group ROLE_PROBATION`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PROBATION" }))

    val reuslt = service.getUserCohort(authentication)

    Assertions.assertThat(reuslt.cohort).isEqualTo(Cohorts.PROBATION)
  }
}
