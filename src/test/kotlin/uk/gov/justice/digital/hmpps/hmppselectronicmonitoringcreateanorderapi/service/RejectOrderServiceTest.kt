package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.EmailClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedNOEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedUserEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.time.ZonedDateTime

class RejectOrderServiceTest {

  @Test
  fun `rejects and saves the order matching the given case id`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderService(gateway, repo, emailClient)

    val order = TestUtilities.createReadyToSubmitOrder()
    gateway.addOrder("CASE123", order)

    service.execute(
      "CASE123",
      ZonedDateTime.now(),
      listOf(RejectionReason(section = "Section A", details = "A details")),
    )

    assertThat(order.status).isEqualTo(OrderStatus.REJECTED)
    assertThat(order.statusUpdates).hasSize(1)
    verify(repo).save(order)
  }

  @Test
  fun `send emails`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderService(gateway, repo, emailClient)

    val order = TestUtilities.createReadyToSubmitOrder(submittedBy = "Bob Jones")
    order.submittedByEmail = "bob.jones@justice.gov.uk"
    order.interestedParties?.notifyingOrganisationEmail = NOTIFYING_ORG_EMAIL
    gateway.addOrder("CASE123", order)

    service.execute(
      "CASE123",
      ZonedDateTime.now(),
      emptyList(),
    )

    verify(emailClient).sendEmail(
      email = RejectedUserEmail(
        emailAddress = "bob.jones@justice.gov.uk",
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        username = "Bob Jones",
      ),
    )
    verify(emailClient).sendEmail(
      email = RejectedNOEmail(
        emailAddress = NOTIFYING_ORG_EMAIL,
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        notifyingOrgName = order.interestedParties?.notifyingOrganisationName,
      ),
    )
  }

  @Test
  fun `does not send a user email when the order has no submitting user email address`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderService(gateway, repo, emailClient)

    val order = TestUtilities.createReadyToSubmitOrder(submittedBy = "Bob Jones")
    order.submittedByEmail = null
    order.interestedParties?.notifyingOrganisationEmail = NOTIFYING_ORG_EMAIL
    gateway.addOrder("CASE123", order)

    service.execute("CASE123", ZonedDateTime.now(), emptyList())

    verify(emailClient, never()).sendEmail(any<RejectedUserEmail>())
    verify(emailClient).sendEmail(any<RejectedNOEmail>())
  }

  @Test
  fun `throws and saves nothing when no order matches the given case id`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderService(gateway, repo, emailClient)

    assertThatThrownBy {
      service.execute("UNKNOWN_CASE_ID", ZonedDateTime.now(), emptyList())
    }.isInstanceOf(EntityNotFoundException::class.java)

    verifyNoInteractions(repo)
  }

  @Test
  fun `does not send notifying org email when no NO email`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val emailClient = mock<EmailClient>()
    val service = RejectOrderService(gateway, repo, emailClient)

    val order = TestUtilities.createReadyToSubmitOrder(submittedBy = "Bob Jones")
    order.submittedByEmail = "bob.jones@justice.gov.uk"
    order.interestedParties?.notifyingOrganisationEmail = null
    gateway.addOrder("CASE123", order)

    service.execute("CASE123", ZonedDateTime.now(), emptyList())

    verify(emailClient).sendEmail(any<RejectedUserEmail>())
    verify(emailClient, never()).sendEmail(any<RejectedNOEmail>())
  }

  private companion object {
    const val NOTIFYING_ORG_EMAIL = "notifying.org@justice.gov.uk"
  }
}
