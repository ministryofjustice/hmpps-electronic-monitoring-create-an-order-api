package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateIsMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import java.util.*

@Service
class MappaService : OrderSectionServiceBase() {
  fun updateMappa(orderId: UUID, username: String, dto: UpdateMappaDto): Mappa {
    val order = this.findEditableOrder(orderId, username)

    order.mappa =
      Mappa(versionId = order.versionId, level = dto.level, isMappa = order.mappa?.isMappa, category = dto.category)

    return orderRepo.save(order).mappa!!
  }

  fun updateIsMappa(orderId: UUID, username: String, dto: UpdateIsMappaDto): Mappa {
    val order = this.findEditableOrder(orderId, username)

    if (order.mappa == null) {
      order.mappa =
        Mappa(versionId = order.versionId, isMappa = dto.isMappa)
    } else {
      if (dto.isMappa == YesNoUnknown.UNKNOWN || dto.isMappa == YesNoUnknown.NO) {
        order.mappa?.level = null
        order.mappa?.category = null
      }
      order.mappa?.isMappa = dto.isMappa
    }

    return orderRepo.save(order).mappa!!
  }
}
