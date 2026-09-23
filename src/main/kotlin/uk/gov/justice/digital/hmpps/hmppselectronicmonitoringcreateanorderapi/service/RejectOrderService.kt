package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZonedDateTime

data class StatusUpdateReasonRequest(val section: String, val details: String)

@Service
class RejectOrderService(private val gateway: OrderByCaseIdGateway, private val repo: OrderRepository) {
  fun execute(caseId: String, dateTime: ZonedDateTime, reasons: List<StatusUpdateReasonRequest>) {
    val order = gateway.findOrderByCaseId(caseId)
      ?: throw EntityNotFoundException("Order with caseId $caseId does not exist")

    order.reject(dateTime, reasons)

    repo.save(order)
  }
}
