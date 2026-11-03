package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.CorrelationIdContext

@Service
class EventService(private val telemetryClient: TelemetryClient) {

  fun recordEvent(eventTitle: String, eventProperties: Map<String, String>? = mapOf(), eventTimeMs: Long = 0) {
    val properties = eventProperties.orEmpty().toMutableMap()
    CorrelationIdContext.current()?.let { properties.putIfAbsent(CorrelationIdContext.MDC_KEY, it) }

    telemetryClient.trackEvent(
      eventTitle,
      properties,
      mapOf("eventTimeMs" to eventTimeMs.toDouble()),
    )
  }
}
