package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.NotifyApiException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedNOEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedUserEmail
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

class NotifyEmailClientTest {

  private val notificationClient = mock<NotificationClient>()

  private val client = NotifyEmailClient(
    notificationClient = notificationClient,
  )

  private val userEmail = RejectedUserEmail(
    emailAddress = USER_EMAIL_ADDRESS,
    dwFirstName = "Alice",
    dwLastName = "Wearer",
    username = "Bob Jones",
  )

  private val organisationEmail = RejectedNOEmail(
    emailAddress = ORGANISATION_EMAIL_ADDRESS,
    dwFirstName = "Alice",
    dwLastName = "Wearer",
    notifyingOrgName = "Probation Service",
  )

  @Test
  fun `sends the user email to the recipient using the user template`() {
    client.sendEmail(userEmail)

    verify(notificationClient).sendEmail(
      eq(USER_TEMPLATE_ID),
      eq(USER_EMAIL_ADDRESS),
      any(),
      isNull(),
    )
  }

  @Test
  fun `sends the user email with exactly the placeholders the template declares`() {
    client.sendEmail(userEmail)

    assertThat(capturedPersonalisation()).containsOnlyKeys(
      "username",
      "dw first name",
      "dw last name",
    ).containsEntry("username", "Bob Jones")
      .containsEntry("dw first name", "Alice")
      .containsEntry("dw last name", "Wearer")
  }

  @Test
  fun `sends the organisation email to the recipient using the organisation template`() {
    client.sendEmail(organisationEmail)

    verify(notificationClient).sendEmail(
      eq(ORGANISATION_TEMPLATE_ID),
      eq(ORGANISATION_EMAIL_ADDRESS),
      any(),
      isNull(),
    )
  }

  @Test
  fun `sends the organisation email with exactly the placeholders the template declares`() {
    client.sendEmail(organisationEmail)

    assertThat(capturedPersonalisation()).containsOnlyKeys(
      "dw first name",
      "dw last name",
      "notifying organisation name",
    ).containsEntry("dw first name", "Alice")
      .containsEntry("dw last name", "Wearer")
      .containsEntry("notifying organisation name", "Probation Service")
  }

  @Test
  fun `translates a Notify failure on the user email into a NotifyApiException`() {
    val cause = NotificationClientException("Notify is unavailable")
    whenever(notificationClient.sendEmail(any(), any(), any(), isNull())).thenThrow(cause)

    assertThatThrownBy { client.sendEmail(userEmail) }
      .isInstanceOf(NotifyApiException::class.java)
      .hasCause(cause)
  }

  @Test
  fun `translates a Notify failure on the organisation email into a NotifyApiException`() {
    val cause = NotificationClientException("Notify is unavailable")
    whenever(notificationClient.sendEmail(any(), any(), any(), isNull())).thenThrow(cause)

    assertThatThrownBy { client.sendEmail(organisationEmail) }
      .isInstanceOf(NotifyApiException::class.java)
      .hasCause(cause)
  }

  @Test
  fun `does not expose personal data in the exception message`() {
    whenever(notificationClient.sendEmail(any(), any(), any(), isNull()))
      .thenThrow(NotificationClientException("Notify is unavailable"))

    assertThatThrownBy { client.sendEmail(userEmail) }
      .hasMessageNotContaining(USER_EMAIL_ADDRESS)
      .hasMessageNotContaining("Alice")
      .hasMessageNotContaining("Wearer")
  }

  private fun capturedPersonalisation(): Map<String, *> {
    val captor = argumentCaptor<Map<String, *>>()
    verify(notificationClient).sendEmail(any(), any(), captor.capture(), isNull())
    return captor.firstValue
  }

  private companion object {
    const val USER_TEMPLATE_ID = "5222afcd-cd14-47a2-ac94-f4e9fb546619"
    const val ORGANISATION_TEMPLATE_ID = "a46310e0-e7f9-43a4-8fde-bc00e3b19c7a"
    const val USER_EMAIL_ADDRESS = "bob.jones@justice.gov.uk"
    const val ORGANISATION_EMAIL_ADDRESS = "notifying.org@justice.gov.uk"
  }
}
