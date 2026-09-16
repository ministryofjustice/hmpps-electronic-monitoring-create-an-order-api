package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@Profile("local")
@EnableWebSecurity
class LocalSecurityConfiguration {

  @Bean
  fun localSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http
      .csrf { csrf -> csrf.disable() }
      .authorizeHttpRequests { auth ->
        auth.anyRequest().permitAll()
      }
      .sessionManagement { sessions ->
        sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      }
      .addFilterBefore(LocalAuthenticationFilter(), AnonymousAuthenticationFilter::class.java)

    return http.build()
  }
}

internal class LocalAuthenticationFilter : OncePerRequestFilter() {
  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    val jwt = Jwt.withTokenValue("local-token")
      .header("alg", "none")
      .claim("user_name", LOCAL_USERNAME)
      .claim("name", "Local User")
      .build()
    val authentication = AuthAwareAuthenticationToken(
      jwt,
      LOCAL_USERNAME,
      "Local User",
      listOf(
        SimpleGrantedAuthority("ROLE_EM_CEMO__CREATE_ORDER"),
        SimpleGrantedAuthority("ROLE_EM_CEMO__GET_ORDER__RO"),
      ),
    )

    try {
      SecurityContextHolder.getContext().authentication = authentication
      filterChain.doFilter(request, response)
    } finally {
      SecurityContextHolder.clearContext()
    }
  }

  companion object {
    private const val LOCAL_USERNAME = "local-user"
  }
}
