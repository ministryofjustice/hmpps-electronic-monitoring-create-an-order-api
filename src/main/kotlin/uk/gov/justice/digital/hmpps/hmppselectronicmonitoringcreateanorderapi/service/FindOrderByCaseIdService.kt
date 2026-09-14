package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderCaseSearchResultDto

interface OrderByCaseIdGateway {
  fun findOrderByCaseId(caseId: String): Order?
}

class FindOrderByCaseIdService(private val gateway: OrderByCaseIdGateway) {
  fun execute(caseId: String): OrderCaseSearchResultDto? =
    gateway.findOrderByCaseId(caseId)?.let { OrderCaseSearchResultDto(id = it.id, versions = it.versions) }
}
