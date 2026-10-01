package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.NotifyProperties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.NotifyApiException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.Email
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.EmailType
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

  private fun getPersonalisation(email: Email): Map<String, String?> =
    email.personalisationFields.mapKeys { (field, _) -> notifyFieldLabels.getValue(field) }

  private fun templateId(emailType: EmailType): String = when (emailType) {
    EmailType.USER -> notifyProperties.templates.orderRejectedUser
    EmailType.NO -> notifyProperties.templates.orderRejectedNo
  }

  companion object {
    private val notifyFieldLabels = mapOf(
      "dwFirstName" to "dw first name",
      "dwLastName" to "dw last name",
      "orderId" to "order id",
      "username" to "username",
      "notifyingOrgName" to "notifying organisation name",
    )
  }
}
