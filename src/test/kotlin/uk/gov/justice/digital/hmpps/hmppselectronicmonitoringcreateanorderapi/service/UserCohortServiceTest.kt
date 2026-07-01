package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.ManageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
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

  @ParameterizedTest(
    name = "It should return true if user cohort is same as original notifying org - {0} -> {1}",
  )
  @MethodSource("userCohortSameAsNotifyingOrgArguments")
  fun `It should return true if user cohort is same as original notifying org`(
    cohort: Cohort,
    notifyingOrganisation: String,
  ) {
    val result = service.matchesNotifyingOrg(cohort, notifyingOrganisation)

    Assertions.assertThat(result).isEqualTo(true)
  }

  @ParameterizedTest(
    name = "It should return false if user cohort is not same as original notifying org - {0} -> {1}",
  )
  @MethodSource("userCohortNotSameAsNotifyingOrgArguments")
  fun `It should not clone NO if user cohort is not same as original notifying org`(
    cohort: Cohort,
    notifyingOrganisation: String,
  ) {
    val result = service.matchesNotifyingOrg(cohort, notifyingOrganisation)

    Assertions.assertThat(result).isEqualTo(false)
  }

  companion object {
    @JvmStatic
    fun userCohortSameAsNotifyingOrgArguments() = listOf(
      Arguments.of(Cohort.PRISON, "PRISON"),
      Arguments.of(Cohort.PRISON, "YOUTH_CUSTODY_SERVICE"),
      Arguments.of(Cohort.PROBATION, "PROBATION"),
      Arguments.of(Cohort.COURT, "CIVIL_COUNTY_COURT"),
      Arguments.of(Cohort.COURT, "CROWN_COURT"),
      Arguments.of(Cohort.COURT, "MAGISTRATES_COURT"),
      Arguments.of(Cohort.COURT, "MILITARY_COURT"),
      Arguments.of(Cohort.COURT, "SCOTTISH_COURT"),
      Arguments.of(Cohort.COURT, "FAMILY_COURT"),
      Arguments.of(Cohort.COURT, "YOUTH_COURT"),
      Arguments.of(Cohort.HOME_OFFICE, "HOME_OFFICE"),
    )

    @JvmStatic
    fun userCohortNotSameAsNotifyingOrgArguments() = listOf(
      Arguments.of(Cohort.PRISON, "PROBATION"),
      Arguments.of(Cohort.PROBATION, "PRISON"),
      Arguments.of(Cohort.PROBATION, "YOUTH_CUSTODY_SERVICE"),
    )
  }
}
