package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.UserDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository

@ActiveProfiles("test")
abstract class OrderSectionServiceTestBase {
  lateinit var repo: OrderRepository
  lateinit var userCohortService: UserCohortService
  lateinit var authentication: JwtAuthenticationToken

  val mockUserDetails = UserDetails(
    username = "AUTH_ADM",
    active = true,
    name = "John Smith",
    authSource = "mockSource",
    userId = "ABC",
    uuid = null,
  )

  fun baseSetup(service: OrderSectionServiceBase) {
    userCohortService = mock()
    authentication = mock(JwtAuthenticationToken::class.java)

    val context = SecurityContextHolder.createEmptyContext()
    context.authentication = authentication
    SecurityContextHolder.setContext(context)

    whenever(userCohortService.getUserCohort(authentication)).thenReturn(UserCohort(Cohort.OTHER))
    whenever(userCohortService.getUserDetails(authentication)).thenReturn(mockUserDetails)

    service.userCohortService = userCohortService
  }
}
