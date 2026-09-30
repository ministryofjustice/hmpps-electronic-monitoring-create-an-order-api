package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.EmailClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.Email
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedNOEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedUserEmail

@Component
@Profile("test")
@Primary
class TestEmailClient : EmailClient {
  private var sentUserEmails = mutableListOf<RejectedUserEmail>()
  private var sentNOEmails = mutableListOf<RejectedNOEmail>()

  override fun sendUserEmail(email: RejectedUserEmail) {
    sentUserEmails.add(email)
  }

  override fun sendNotificationOfficerEmail(email: RejectedNOEmail) {
    sentNOEmails.add(email)
  }

  fun hasSentUserEmail(order: Order): Boolean {
    val email = sentUserEmails.first()

    return dwNameMatches(email, order) && usernameMatches(email, order)
  }

  fun hasSentNOEmail(order: Order): Boolean {
    val email = sentNOEmails.first()
    return dwNameMatches(email, order)
  }

  // TODO
//  private fun emailAddressMatches(email: Email, order: Order): Boolean {
//    return email.emailAddress == order.submittedByEmail
//  }

  private fun dwNameMatches(email: Email, order: Order): Boolean =
    email.dwFirstName == order.deviceWearer?.firstName && email.dwLastName == order.deviceWearer?.lastName

  private fun usernameMatches(email: RejectedUserEmail, order: Order): Boolean =
    "${email.userFirstName} ${email.userLastName}" == order.submittedBy
}
