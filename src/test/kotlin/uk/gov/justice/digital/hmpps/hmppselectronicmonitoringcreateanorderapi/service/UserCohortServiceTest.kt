package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Cohorts

@ActiveProfiles("test")
@JsonTest
class UserCohortServiceTest {

  private lateinit var authentication: Authentication
  private lateinit var submitAuthentication: AuthAwareAuthenticationToken

  private lateinit var service: UserCohortService

  @BeforeEach
  fun setup() {
    authentication = mock(Authentication::class.java)
    submitAuthentication = mock(AuthAwareAuthenticationToken::class.java)
    service = UserCohortService()
  }

  @Test
  fun `Should return cohort of prison when user has role ROLE_PRISON`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PRISON" }))

    val reuslt = service.getUserCohort(authentication)

    Assertions.assertThat(reuslt.cohort).isEqualTo(Cohorts.PRISON)
  }

  @Test
  fun `Should return cohort of probation when user has role ROLE_PROBATION`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PROBATION" }))

    val reuslt = service.getUserCohort(authentication)

    Assertions.assertThat(reuslt.cohort).isEqualTo(Cohorts.PROBATION)
  }
}
