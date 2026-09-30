package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZonedDateTime

@Service
class RejectOrderService(private val gateway: OrderByCaseIdGateway, private val repo: OrderRepository) {
  @Transactional
  fun execute(caseId: String, dateTime: ZonedDateTime, reasons: List<RejectionReason>) {
    val order = gateway.findOrderByCaseId(caseId)
      ?: throw EntityNotFoundException("Order with caseId $caseId does not exist")

    order.reject(dateTime, reasons)

    repo.save(order)
  }
}
