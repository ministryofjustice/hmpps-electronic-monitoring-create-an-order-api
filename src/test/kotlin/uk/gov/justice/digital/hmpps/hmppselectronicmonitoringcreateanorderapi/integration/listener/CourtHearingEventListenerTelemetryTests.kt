package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.listener

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Test
import org.mockito.internal.verification.Times
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener.CourtHearingEventListener

@Transactional
class CourtHearingEventListenerTelemetryTests : CourtHearingEventListenerTest() {

  @Autowired
  lateinit var courtHearingEventListener: CourtHearingEventListener

  @SpyBean
  lateinit var telemetryClient: TelemetryClient

  @Test
  fun `Will log event for malformed payload`() {
    courtHearingEventListener.onDomainEvent("BAD JSON")

    verify(telemetryClient, Times(1)).trackEvent(eq("Common_Platform_Exception"), any(), any())
    verify(telemetryClient, Times(1)).trackEvent(eq("Common_Platform_Request"), any(), any())
  }

  @Test
  fun `Will only log request event has no EM request`() {
    val rawMessage = generateRawHearingEventMessage("src/test/resources/json/No_EM_Payload.json")
    courtHearingEventListener.onDomainEvent(rawMessage)
    verify(telemetryClient, Times(0)).trackEvent(eq("Common_Platform_Success_Request"), any(), any())
    verify(telemetryClient, Times(0)).trackEvent(eq("Common_Platform_Failed_Request"), any(), any())
    verify(telemetryClient, Times(0)).trackEvent(eq("Common_Platform_Exception"), any(), any())
    verify(telemetryClient, Times(1)).trackEvent(eq("Common_Platform_Request"), any(), any())
  }

  @Test
  fun `Will log success and request event for valid em payload`() {
    val rawMessage = generateRawHearingEventMessage("src/test/resources/json/COEW_AAR/cp_payload.json")
    courtHearingEventListener.onDomainEvent(rawMessage)

    verify(telemetryClient, Times(1)).trackEvent(eq("Common_Platform_Success_Request"), any(), any())
    verify(telemetryClient, Times(0)).trackEvent(eq("Common_Platform_Failed_Request"), any(), any())
    verify(telemetryClient, Times(0)).trackEvent(eq("Common_Platform_Exception"), any(), any())
    verify(telemetryClient, Times(1)).trackEvent(eq("Common_Platform_Request"), any(), any())
  }
}
