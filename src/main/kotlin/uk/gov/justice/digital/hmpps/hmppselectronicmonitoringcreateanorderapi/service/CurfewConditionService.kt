package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import java.util.*

@Service
class CurfewConditionService : OrderSectionServiceBase() {
  fun updateCurfewCondition(orderId: UUID, username: String, condition: CurfewConditions) {
    val order = findEditableOrder(orderId, username)
    order.curfewConditions = condition
    orderRepo.save(order)
  }
}
