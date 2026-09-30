package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import org.assertj.core.api.Assertions.assertThat
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
  private val userEmails = mutableListOf<RejectedUserEmail>()
  private val noEmails = mutableListOf<RejectedNOEmail>()

  override fun sendUserEmail(email: RejectedUserEmail) {
    userEmails.add(email)
  }

  override fun sendNotificationOfficerEmail(email: RejectedNOEmail) {
    noEmails.add(email)
  }

  fun reset() {
    userEmails.clear()
    noEmails.clear()
  }

  fun assertSentUserEmail(expected: RejectedUserEmail) {
    assertThat(userEmails).containsExactly(expected)
  }

  fun assertSentNoUserEmails() {
    assertThat(userEmails).isEmpty()
  }

  fun assertSentNotificationOfficerEmail(expected: RejectedNOEmail) {
    assertThat(noEmails).containsExactly(expected)
  }
}
