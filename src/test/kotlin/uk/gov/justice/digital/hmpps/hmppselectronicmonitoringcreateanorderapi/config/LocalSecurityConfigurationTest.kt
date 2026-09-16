package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import jakarta.servlet.FilterChain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Profile
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class LocalSecurityConfigurationTest {

  @Test
  fun `local profile is enabled for the local security configuration`() {
    val profile = LocalSecurityConfiguration::class.java.getAnnotation(Profile::class.java)

    assertThat(profile.value).containsExactly("local")
  }

  @Test
  fun `local authentication filter supplies a local JWT authentication`() {
    var authenticationName: String? = null
    val filterChain = FilterChain { _, _ ->
      authenticationName = SecurityContextHolder.getContext().authentication?.name
    }

    LocalAuthenticationFilter().doFilter(
      MockHttpServletRequest(),
      MockHttpServletResponse(),
      filterChain,
    )

    assertThat(authenticationName).isEqualTo("local-user")
    assertThat(SecurityContextHolder.getContext().authentication).isNull()
  }
}
