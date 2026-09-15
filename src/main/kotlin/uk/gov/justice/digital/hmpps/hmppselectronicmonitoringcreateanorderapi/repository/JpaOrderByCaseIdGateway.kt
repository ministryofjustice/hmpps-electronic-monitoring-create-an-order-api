package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OrderByCaseIdGateway

@Component
class JpaOrderByCaseIdGateway(
  private val fmsSubmissionResultRepository: FmsSubmissionResultRepository,
  private val orderRepository: OrderRepository,
) : OrderByCaseIdGateway {
  override fun findOrderByCaseId(caseId: String): Order? = fmsSubmissionResultRepository.findByCaseId(caseId)
    ?.let { orderRepository.findById(it.orderId).orElse(null) }
}
