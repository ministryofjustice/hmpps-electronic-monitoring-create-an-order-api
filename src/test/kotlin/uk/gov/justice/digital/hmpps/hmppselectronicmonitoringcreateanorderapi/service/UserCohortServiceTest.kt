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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsCaseload

@ActiveProfiles("test")
@JsonTest
class UserCohortServiceTest {

  private lateinit var authentication: JwtAuthenticationToken
  private lateinit var mockJwtToken: Jwt
  private lateinit var mockApi: ManageUserApi
  private lateinit var service: UserCohortService

  @BeforeEach
  fun setup() {
    authentication = mock(JwtAuthenticationToken::class.java)
    mockJwtToken = mock(Jwt::class.java)
    `when`(authentication.token).thenReturn(mockJwtToken)

    mockApi = mock()
    service = UserCohortService(mockApi)
  }

  @Test
  fun `Should return user cohort of prison when user has role ROLE_PRISON`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PRISON" }))
    `when`(mockJwtToken.tokenValue).thenReturn("Mock Token")
    `when`(mockApi.getUserActiveCaseload(authentication.token)).thenReturn(
      HmppsCaseload(name = "HMP ABC", id = "ABC"),
    )
    val result = service.getUserCohort(authentication)

    Assertions.assertThat(result.cohort).isEqualTo(Cohort.PRISON)
    Assertions.assertThat(result.activeCaseLoadName).isEqualTo("HMP ABC")
    Assertions.assertThat(result.activeCaseLoadId).isEqualTo("ABC")
  }

  @Test
  fun `Should return cohort of probation when user has role ROLE_PROBATION`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_PROBATION" }))

    val result = service.getUserCohort(authentication)

    Assertions.assertThat(result.cohort).isEqualTo(Cohort.PROBATION)
  }

  @Test
  fun `returns court when user has court groups`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_EM_CEMO_COURT" }))
    val result = service.getUserCohort(authentication)

    Assertions.assertThat(result.cohort).isEqualTo(Cohort.COURT)
  }

  @Test
  fun `returns home office when user has HO groups`() {
    `when`(authentication.authorities).thenReturn(listOf(GrantedAuthority { "ROLE_EM_CEMO_HOME_OFFICE" }))

    val result = service.getUserCohort(authentication)

    Assertions.assertThat(result.cohort).isEqualTo(Cohort.HOME_OFFICE)
  }

  @Test
  fun `matchesNotifyingOrg returns true both prison`() {
    val testNotifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
    val testCohort = UserCohort(cohort = Cohort.PRISON)

    val result = service.matchesNofifyingOrg(testCohort, testNotifyingOrganisation)

    Assertions.assertThat(result).isEqualTo(true)
  }

  @Test
  fun `matchesNotifyingOrg returns true both probation`() {
    val testNotifyingOrganisation = NotifyingOrganisationDDv5.PROBATION.name
    val testCohort = UserCohort(cohort = Cohort.PROBATION)

    val result = service.matchesNofifyingOrg(testCohort, testNotifyingOrganisation)

    Assertions.assertThat(result).isEqualTo(true)
  }

  @Test
  fun `matchesNotifyingOrg returns false prison probation`() {
    val testNotifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
    val testCohort = UserCohort(cohort = Cohort.PROBATION)

    val result = service.matchesNofifyingOrg(testCohort, testNotifyingOrganisation)

    Assertions.assertThat(result).isEqualTo(false)
  }

  @Test
  fun `matchesNotifyingOrg returns true court court`() {
    val testNotifyingOrganisation = NotifyingOrganisationDDv5.CIVIL_COUNTY_COURT.name
    val testCohort = UserCohort(cohort = Cohort.COURT)

    val result = service.matchesNofifyingOrg(testCohort, testNotifyingOrganisation)

    Assertions.assertThat(result).isEqualTo(true)
  }

  @Test
  fun `matchesNotifyingOrg returns true home office home office`() {
    val testNotifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE.name
    val testCohort = UserCohort(cohort = Cohort.HOME_OFFICE)

    val result = service.matchesNofifyingOrg(testCohort, testNotifyingOrganisation)

    Assertions.assertThat(result).isEqualTo(true)
  }

  @Test
  fun `matchesNotifyingOrg returns false when cohort is other`() {
    val testNotifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE.name
    val testCohort = UserCohort(cohort = Cohort.OTHER)

    val result = service.matchesNofifyingOrg(testCohort, testNotifyingOrganisation)

    Assertions.assertThat(result).isEqualTo(false)
  }
}
