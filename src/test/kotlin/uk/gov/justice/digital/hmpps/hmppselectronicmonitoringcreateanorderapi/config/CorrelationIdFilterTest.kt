package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import jakarta.servlet.FilterChain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class CorrelationIdFilterTest {
  private val filter = CorrelationIdFilter()

  @AfterEach
  fun clearMdc() {
    MDC.clear()
  }

  @Test
  fun `adds valid request correlation id to MDC and response`() {
    val request = MockHttpServletRequest().apply {
      addHeader(CorrelationIdContext.HEADER_NAME, "request-123")
    }
    val response = MockHttpServletResponse()
    var observedCorrelationId: String? = null
    val chain = FilterChain { _, _ -> observedCorrelationId = MDC.get(CorrelationIdContext.MDC_KEY) }

    filter.doFilter(request, response, chain)

    assertThat(observedCorrelationId).isEqualTo("request-123")
    assertThat(response.getHeader(CorrelationIdContext.HEADER_NAME)).isEqualTo("request-123")
    assertThat(MDC.get(CorrelationIdContext.MDC_KEY)).isNull()
  }

  @Test
  fun `replaces invalid request correlation id`() {
    val request = MockHttpServletRequest().apply {
      addHeader(CorrelationIdContext.HEADER_NAME, "invalid value\r\n")
    }
    val response = MockHttpServletResponse()
    val chain = FilterChain { _, _ -> }

    filter.doFilter(request, response, chain)

    assertThat(response.getHeader(CorrelationIdContext.HEADER_NAME))
      .matches("[A-Fa-f0-9-]{36}")
  }
}
