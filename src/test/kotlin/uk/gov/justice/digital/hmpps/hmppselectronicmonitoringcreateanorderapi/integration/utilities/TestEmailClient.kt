package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.EmailClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedNOEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedUserEmail

@Component
@Profile("test")
@Primary
class TestEmailClient : EmailClient {
  private var sendUserEmail = mutableListOf<RejectedUserEmail>()
  private var sendNOEmail = mutableListOf<RejectedNOEmail>()

  override fun sendUserEmail(email: RejectedUserEmail) {
    sendUserEmail.add(email)
  }

  override fun sendNotificationOfficerEmail(email: RejectedNOEmail) {
    sendNOEmail.add(email)
  }

  fun hasSentUserEmail(): Boolean = sendUserEmail.size == 1

  fun hasSentNOEmail(): Boolean = sendNOEmail.size == 1
}
