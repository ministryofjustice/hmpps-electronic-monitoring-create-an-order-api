package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.EmailClient

@Component
@Profile("test")
@Primary
class TestEmailClient : EmailClient {
  private var hasUserEmailSent = false
  private var hasNOEmailSent = false

  override fun sendUserEmail() {
    hasUserEmailSent = true
  }

  override fun sendNotificationOfficerEmail() {
    hasNOEmailSent = true
  }

  fun hasRecievedUserEmail(): Boolean = hasUserEmailSent

  fun hasRecievedNOEmail(): Boolean = hasNOEmailSent
}
