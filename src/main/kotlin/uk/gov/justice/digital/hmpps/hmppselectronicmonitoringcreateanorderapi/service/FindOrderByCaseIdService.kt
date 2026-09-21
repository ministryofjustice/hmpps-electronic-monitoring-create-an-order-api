package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderCaseSearchResultDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.toOrderVersionDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository

interface OrderByCaseIdGateway {
  fun findOrderByCaseId(caseId: String): Order?
}

@Service
class FindOrderByCaseIdService(private val gateway: OrderByCaseIdGateway) {
  fun execute(caseId: String): OrderCaseSearchResultDto? = gateway.findOrderByCaseId(caseId)?.let {
    OrderCaseSearchResultDto(id = it.id, versions = it.versions.map { version -> version.toOrderVersionDto() })
  }
}

@Service
class UpdateOrderStatusService(private val gateway: OrderByCaseIdGateway, private val repo: OrderRepository) {
  fun execute(caseId: String, status: OrderStatus) {
    val order = gateway.findOrderByCaseId(caseId)

    order!!.status = status

    repo.save(order)
  }
}
