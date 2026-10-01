package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import org.assertj.core.api.Assertions.assertThat
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.EmailClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.Email

@Component
@Profile("test")
@Primary
class TestEmailClient : EmailClient {
  private val emails = mutableListOf<Email>()
  private var failing = false

  override fun sendEmail(email: Email) {
    if (failing) throw RuntimeException("Simulated Notify failure")
    emails.add(email)
  }

  fun reset() {
    emails.clear()
    failing = false
  }

  fun failAlways() {
    failing = true
  }

  fun assertSent(vararg expected: Email) {
    assertThat(emails).containsExactlyInAnyOrder(*expected)
  }

  fun assertNothingSent() {
    assertThat(emails).isEmpty()
  }
}
