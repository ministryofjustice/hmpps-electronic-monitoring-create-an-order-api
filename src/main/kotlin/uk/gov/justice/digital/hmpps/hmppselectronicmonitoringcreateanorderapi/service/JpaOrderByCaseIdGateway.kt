package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository

@Component
class JpaOrderByCaseIdGateway(
  private val fmsSubmissionResultRepository: FmsSubmissionResultRepository,
  private val orderRepository: OrderRepository,
) : OrderByCaseIdGateway {
  override fun findOrderByCaseId(caseId: String): Order? = fmsSubmissionResultRepository.findByCaseId(caseId)
    ?.let { orderRepository.findById(it.orderId).orElse(null) }
}
