package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.CorrelationIdContext

class EventServiceTest {
  private val telemetryClient: TelemetryClient = mock()
  private val eventService = EventService(telemetryClient)

  @Test
  fun `adds the active correlation id to event properties`() {
    CorrelationIdContext.withCorrelationId("request-123") {
      eventService.recordEvent("test_event")
    }

    val properties = argumentCaptor<Map<String, String>>()
    verify(telemetryClient).trackEvent(eq("test_event"), properties.capture(), org.mockito.kotlin.any())

    assertThat(properties.firstValue).containsEntry(CorrelationIdContext.MDC_KEY, "request-123")
  }
}
