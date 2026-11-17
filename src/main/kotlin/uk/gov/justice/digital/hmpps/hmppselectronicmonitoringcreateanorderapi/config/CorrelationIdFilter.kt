package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : OncePerRequestFilter() {
  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    val correlationId = CorrelationIdContext.validOrNew(request.getHeader(CorrelationIdContext.HEADER_NAME))
    response.setHeader(CorrelationIdContext.HEADER_NAME, correlationId)

    CorrelationIdContext.withCorrelationId(correlationId) {
      filterChain.doFilter(request, response)
    }
  }
}
