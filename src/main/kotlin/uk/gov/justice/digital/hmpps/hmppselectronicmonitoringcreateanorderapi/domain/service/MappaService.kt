package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.domain.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.domain.model.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.ports.out.GetOrderPort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.ports.out.UpdateOrderPort
import java.util.*

@Service
class MappaService(private val getOrderPort: GetOrderPort, private val updateOrderPort: UpdateOrderPort) {

  fun updateMappa(orderId: UUID, username: String, dto: UpdateMappaDto): Mappa {
    val order = this.getOrderPort.getOrderById(orderId, username)

    if (order.mappa != null) {
      order.mappa?.update(dto.level, dto.category)
    } else {
      order.addMappa(Mappa(versionId = order.versionId, level = dto.level, category = dto.category))
    }

    this.updateOrderPort.updateOrder(order)

    return order.mappa!!
  }
}
