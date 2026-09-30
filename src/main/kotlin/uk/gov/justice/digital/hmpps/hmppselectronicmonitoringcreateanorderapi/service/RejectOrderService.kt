package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.EmailClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedNOEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedUserEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZonedDateTime

@Service
class RejectOrderService(
  private val gateway: OrderByCaseIdGateway,
  private val repo: OrderRepository,
  private val emailClient: EmailClient,
) {
  @Transactional
  fun execute(caseId: String, dateTime: ZonedDateTime, reasons: List<RejectionReason>) {
    val order = gateway.findOrderByCaseId(caseId)
      ?: throw EntityNotFoundException("Order with caseId $caseId does not exist")

    order.reject(dateTime, reasons)

    repo.save(order)

    sendUserEmail(order)

    emailClient.sendNotificationOfficerEmail(
      email = RejectedNOEmail(
        emailAddress = order.interestedParties?.notifyingOrganisationEmail,
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        notifyingOrgName = order.interestedParties?.notifyingOrganisationName,
      ),
    )
  }

  private fun sendUserEmail(order: Order) {
    val emailAddress = order.submittedByEmail?.takeIf { it.isNotBlank() } ?: return

    emailClient.sendUserEmail(
      email = RejectedUserEmail(
        emailAddress = emailAddress,
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        userFirstName = order.submittedBy?.split(" ")?.first(),
        userLastName = order.submittedBy?.split(" ")?.last(),
      ),
    )
  }
}
