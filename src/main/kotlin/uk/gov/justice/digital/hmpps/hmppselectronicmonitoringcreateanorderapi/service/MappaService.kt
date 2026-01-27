package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMappaDto
import java.util.*

@Service
class MappaService : OrderSectionServiceBase() {
  fun updateMappa(orderId: UUID, username: String, dto: UpdateMappaDto): Mappa {
    val order = this.findEditableOrder(orderId, username)

    order.mappa = Mappa(versionId = order.versionId, level = dto.level, category = dto.category)

    return orderRepo.save(order).mappa!!
  }
}
