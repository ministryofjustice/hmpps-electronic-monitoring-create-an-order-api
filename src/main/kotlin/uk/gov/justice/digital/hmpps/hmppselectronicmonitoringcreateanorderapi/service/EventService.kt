package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service

@Service
class EventService(
  private val telemetryClient: TelemetryClient,
) {

  fun recordEvent(eventTitle: String, eventProperties: Map<String, String>? = mapOf(), eventTimeMs: Long = 0) {
    telemetryClient.trackEvent(
      eventTitle,
      eventProperties,
      mapOf("eventTimeMs" to eventTimeMs.toDouble()),
    )
  }
}
