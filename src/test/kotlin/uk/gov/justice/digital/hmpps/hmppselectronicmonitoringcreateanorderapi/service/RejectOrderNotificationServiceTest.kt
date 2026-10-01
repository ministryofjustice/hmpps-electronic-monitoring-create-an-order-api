package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.EmailClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedNOEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedUserEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.time.ZonedDateTime

class RejectOrderNotificationServiceTest {

  @Test
  fun `propagates the exception when the order cannot be rejected`() {
    val rejectOrderService = mock<RejectOrderService>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderNotificationService(rejectOrderService, emailClient)

    val dateTime = ZonedDateTime.now()
    val reasons = listOf(RejectionReason(section = "Section A", details = "A details"))
    whenever(rejectOrderService.execute("CASE123", dateTime, reasons))
      .thenThrow(EntityNotFoundException("Order with caseId CASE123 does not exist"))

    assertThatThrownBy {
      service.execute("CASE123", dateTime, reasons)
    }.isInstanceOf(EntityNotFoundException::class.java)
  }

  @Test
  fun `sends the user email for the rejected order`() {
    val rejectOrderService = mock<RejectOrderService>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderNotificationService(rejectOrderService, emailClient)

    val order = TestUtilities.createReadyToSubmitOrder(submittedBy = "Bob Jones")
    order.submittedByEmail = "bob.jones@justice.gov.uk"

    val dateTime = ZonedDateTime.now()
    val reasons = listOf(RejectionReason(section = "Section A", details = "A details"))
    whenever(rejectOrderService.execute("CASE123", dateTime, reasons)).thenReturn(order)

    service.execute("CASE123", dateTime, reasons)

    verify(emailClient).sendEmail(
      RejectedUserEmail(
        emailAddress = "bob.jones@justice.gov.uk",
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        username = "Bob Jones",
      ),
    )
  }

  @Test
  fun `sends the notifying organisation email for the rejected order`() {
    val rejectOrderService = mock<RejectOrderService>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderNotificationService(rejectOrderService, emailClient)

    val order = TestUtilities.createReadyToSubmitOrder(submittedBy = "Bob Jones")
    order.submittedByEmail = "bob.jones@justice.gov.uk"
    order.interestedParties?.notifyingOrganisationEmail = "notify.org@justice.gov.uk"
    order.interestedParties?.notifyingOrganisationName = "Probation Service"

    val dateTime = ZonedDateTime.now()
    val reasons = listOf(RejectionReason(section = "Section A", details = "A details"))
    whenever(rejectOrderService.execute("CASE123", dateTime, reasons)).thenReturn(order)

    service.execute("CASE123", dateTime, reasons)

    verify(emailClient).sendEmail(
      RejectedNOEmail(
        emailAddress = "notify.org@justice.gov.uk",
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        notifyingOrgName = "Probation Service",
      ),
    )
  }

  @Test
  fun `does not throw when sending the user email fails`() {
    val rejectOrderService = mock<RejectOrderService>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderNotificationService(rejectOrderService, emailClient)

    val order = TestUtilities.createReadyToSubmitOrder(submittedBy = "Bob Jones")
    order.submittedByEmail = "bob.jones@justice.gov.uk"

    val dateTime = ZonedDateTime.now()
    val reasons = listOf(RejectionReason(section = "Section A", details = "A details"))
    whenever(rejectOrderService.execute("CASE123", dateTime, reasons)).thenReturn(order)
    whenever(emailClient.sendEmail(any<RejectedUserEmail>())).thenThrow(RuntimeException("Notify is down"))

    assertThatCode {
      service.execute("CASE123", dateTime, reasons)
    }.doesNotThrowAnyException()
  }

  @Test
  fun `still attempts the notifying organisation email when the user email fails`() {
    val rejectOrderService = mock<RejectOrderService>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderNotificationService(rejectOrderService, emailClient)

    val order = TestUtilities.createReadyToSubmitOrder(submittedBy = "Bob Jones")
    order.submittedByEmail = "bob.jones@justice.gov.uk"
    order.interestedParties?.notifyingOrganisationEmail = "notify.org@justice.gov.uk"
    order.interestedParties?.notifyingOrganisationName = "Probation Service"

    val dateTime = ZonedDateTime.now()
    val reasons = listOf(RejectionReason(section = "Section A", details = "A details"))
    whenever(rejectOrderService.execute("CASE123", dateTime, reasons)).thenReturn(order)
    whenever(emailClient.sendEmail(any<RejectedUserEmail>())).thenThrow(RuntimeException("Notify is down"))

    service.execute("CASE123", dateTime, reasons)

    verify(emailClient).sendEmail(
      RejectedNOEmail(
        emailAddress = "notify.org@justice.gov.uk",
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        notifyingOrgName = "Probation Service",
      ),
    )
  }

  @Test
  fun `does not throw when sending the notifying organisation email fails`() {
    val rejectOrderService = mock<RejectOrderService>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderNotificationService(rejectOrderService, emailClient)

    val order = TestUtilities.createReadyToSubmitOrder(submittedBy = "Bob Jones")
    order.interestedParties?.notifyingOrganisationEmail = "notify.org@justice.gov.uk"
    order.interestedParties?.notifyingOrganisationName = "Probation Service"

    val dateTime = ZonedDateTime.now()
    val reasons = listOf(RejectionReason(section = "Section A", details = "A details"))
    whenever(rejectOrderService.execute("CASE123", dateTime, reasons)).thenReturn(order)
    whenever(emailClient.sendEmail(any<RejectedNOEmail>())).thenThrow(RuntimeException("Notify is down"))

    assertThatCode {
      service.execute("CASE123", dateTime, reasons)
    }.doesNotThrowAnyException()
  }
}
