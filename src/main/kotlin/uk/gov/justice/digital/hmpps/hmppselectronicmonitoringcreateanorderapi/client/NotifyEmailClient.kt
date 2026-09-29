package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.stereotype.Component

@Component
class NotifyEmailClient : EmailClient {
  override fun sendUserEmail() {
    TODO("Not yet implemented")
  }

  override fun sendNotificationOfficerEmail() {
    TODO("Not yet implemented")
  }
}
