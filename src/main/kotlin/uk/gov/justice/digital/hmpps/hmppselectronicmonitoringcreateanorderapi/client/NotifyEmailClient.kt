package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.NotifyProperties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.NotifyApiException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.Email
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.EmailType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedNOEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedUserEmail
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

@Component
@EnableConfigurationProperties(
  NotifyProperties::class,
)
class NotifyEmailClient(
  private val notificationClient: NotificationClient,
  private val notifyProperties: NotifyProperties,
) : EmailClient {
  override fun sendEmail(email: Email) {
    try {
      notificationClient.sendEmail(
        templateId(email.type),
        email.emailAddress,
        getPersonalisation(email),
        null,
      )
    } catch (e: NotificationClientException) {
      throw NotifyApiException("Failed to submit email", cause = e)
    }
  }

  private fun getPersonalisation(email: Email): Map<String, String?> = when (email) {
    is RejectedUserEmail ->
      mapOf("dw first name" to email.dwFirstName, "dw last name" to email.dwLastName, "username" to email.username)

    is RejectedNOEmail ->
      mapOf(
        "dw first name" to email.dwFirstName,
        "dw last name" to email.dwLastName,
        "notifying organisation name" to email.notifyingOrgName,
      )

    else -> mapOf()
  }

  private fun templateId(emailType: EmailType): String = when (emailType) {
    EmailType.USER -> notifyProperties.templates.orderRejectedUser
    EmailType.NO -> notifyProperties.templates.orderRejectedNo
  }
}
