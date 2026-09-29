package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedNOEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedUserEmail

@Component
class NotifyEmailClient : EmailClient {
  override fun sendUserEmail(email: RejectedUserEmail) {
    TODO("Not yet implemented")
  }

  override fun sendNotificationOfficerEmail(email: RejectedNOEmail) {
    TODO("Not yet implemented")
  }
}
