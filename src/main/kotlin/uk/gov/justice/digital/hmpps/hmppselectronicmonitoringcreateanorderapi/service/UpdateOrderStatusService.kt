package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository

@Service
class UpdateOrderStatusService(private val gateway: OrderByCaseIdGateway, private val repo: OrderRepository) {
  fun execute(caseId: String, status: OrderStatus) {
    val order = gateway.findOrderByCaseId(caseId)
      ?: throw EntityNotFoundException("Order with caseId $caseId does not exist")

    order.status = status

    repo.save(order)
  }
}
