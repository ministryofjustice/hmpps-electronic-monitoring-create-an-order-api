package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.EmailClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.Email
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedNOEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.RejectedUserEmail
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZonedDateTime

@Service
class RejectOrderNotificationService(
  private val rejectOrderService: RejectOrderService,
  private val orderRepository: OrderRepository,
  private val emailClient: EmailClient,
) {
  @Transactional
  fun execute(caseId: String, dateTime: ZonedDateTime, reasons: List<RejectionReason>) {
    val orderId = rejectOrderService.execute(caseId, dateTime, reasons)
    val order = orderRepository.findById(orderId).get()

    RejectedUserEmail.fromOrder(order)?.let { sendEmailAndLogFailure(it) }
    RejectedNOEmail.fromOrder(order)?.let { sendEmailAndLogFailure(it) }
  }

  private fun sendEmailAndLogFailure(email: Email) {
    try {
      emailClient.sendEmail(email)
    } catch (e: Exception) {
      log.error("Failed to send rejection email of type ${email.type}: ${e.message}")
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
