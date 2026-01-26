package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMappaDto
import java.util.*

@Service
class MappaService : OrderSectionServiceBase() {
  fun updateMappa(mockOrderId: UUID, mockUsername: String, mockUpdateDto: UpdateMappaDto): Mappa {
    val order = this.findEditableOrder(mockOrderId, mockUsername)

    order.mappa = Mappa(versionId = order.versionId, level = mockUpdateDto.level, category = mockUpdateDto.category)

    return orderRepo.save(order).mappa!!
  }
}
