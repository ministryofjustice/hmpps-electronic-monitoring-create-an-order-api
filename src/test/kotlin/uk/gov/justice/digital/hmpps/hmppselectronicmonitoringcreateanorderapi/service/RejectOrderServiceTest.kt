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
    gateway.addOrder("CASE123", order)

    service.execute(
      "CASE123",
      ZonedDateTime.now(),
      emptyList(),
    )

    verify(emailClient).sendUserEmail(
      email = RejectedUserEmail(
        emailAddress = "bob.jones@justice.gov.uk",
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        userFirstName = "Bob",
        userLastName = "Jones",
      ),
    )
    verify(emailClient).sendNotificationOfficerEmail(
      email = RejectedNOEmail(
        emailAddress = order.interestedParties?.notifyingOrganisationEmail,
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
    gateway.addOrder("CASE123", order)

    service.execute("CASE123", ZonedDateTime.now(), emptyList())

    verify(emailClient, never()).sendUserEmail(any())
    verify(emailClient).sendNotificationOfficerEmail(any())
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
}
