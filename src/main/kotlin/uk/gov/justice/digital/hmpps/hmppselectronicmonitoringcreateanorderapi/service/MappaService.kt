package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderParameters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateIsMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMappaDto
import java.util.*

@Service
class MappaService : OrderSectionServiceBase() {
  fun updateMappa(orderId: UUID, username: String, dto: UpdateMappaDto): Mappa {
    val order = this.findEditableOrder(orderId, username)

    order.mappa = Mappa(versionId = order.versionId, level = dto.level, category = dto.category)

    return orderRepo.save(order).mappa!!
  }

  fun updateIsMappa(orderId: UUID, username: String, dto: UpdateIsMappaDto): OrderParameters {
    val order = this.findEditableOrder(orderId, username)

    if (order.orderParameters == null) {
      order.orderParameters =
        OrderParameters(versionId = order.getCurrentVersion().id, isMappa = dto.isMappa)
    } else {
      order.orderParameters?.isMappa = dto.isMappa
    }

    return orderRepo.save(order).orderParameters!!
  }
}
