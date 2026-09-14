package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order

interface OrderByCaseIdGateway {
  fun findOrderByCaseId(caseId: String): Order?
}

class FindOrderByCaseIdService(private val gateway: OrderByCaseIdGateway) {
  fun execute(caseId: String): Order? = gateway.findOrderByCaseId(caseId)
}
