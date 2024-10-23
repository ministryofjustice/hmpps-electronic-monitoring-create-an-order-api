package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import java.util.*

@Service
class CurfewReleaseDateService : OrderSectionServiceBase() {
  fun updateCurfewReleaseDateCondition(orderId: UUID, username: String, releaseDate: CurfewReleaseDateConditions) {
    val order = findEditableOrder(orderId, username)
    order.curfewReleaseDateConditions = releaseDate
    orderRepo.save(order)
  }
}
