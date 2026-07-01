package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository

@ActiveProfiles("test")
abstract class OrderSectionServiceTestBase {
  lateinit var authentication: AuthAwareAuthenticationToken
  lateinit var repo: OrderRepository

  @BeforeEach
  fun baseSetup() {
    authentication = mock(AuthAwareAuthenticationToken::class.java)
    whenever(authentication.name).thenReturn("John Smith")
    val context = SecurityContextHolder.createEmptyContext()
    context.authentication = authentication
    SecurityContextHolder.setContext(context)
  }
}
