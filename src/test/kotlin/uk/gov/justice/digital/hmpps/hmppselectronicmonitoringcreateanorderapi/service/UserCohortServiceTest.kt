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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.ManageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohorts

@ActiveProfiles("test")
@JsonTest
class UserCohortServiceTest {

  private lateinit var authentication: JwtAuthenticationToken
  private lateinit var jwtToken: Jwt
  private lateinit var mockApi: ManageUserApi
  private lateinit var service: UserCohortService

  @BeforeEach
  fun setup() {
    authentication = mock(JwtAuthenticationToken::class.java)
    jwtToken = mock(Jwt::class.java)
    `when`(authentication.token).thenReturn(jwtToken)

    mockApi = mock()
    service = UserCohortService(mockApi)
  }

  @Test
  fun `Should return user cohort of prison when user has role ROLE_PRISON`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PRISON" }))
    `when`(jwtToken.tokenValue).thenReturn("Mock Token")
    `when`(mockApi.getUserActiveCaseload(authentication.token)).thenReturn(
      "HMP ABC",
    )
    val result = service.getUserCohort(authentication)

    Assertions.assertThat(result.cohort).isEqualTo(Cohorts.PRISON)
    Assertions.assertThat(result.activeCaseLoad).isEqualTo("HMP ABC")
  }

  @Test
  fun `Should return cohort of probation when user has role ROLE_PROBATION`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PROBATION" }))

    val result = service.getUserCohort(authentication)

    Assertions.assertThat(result.cohort).isEqualTo(Cohorts.PROBATION)
  }
}
