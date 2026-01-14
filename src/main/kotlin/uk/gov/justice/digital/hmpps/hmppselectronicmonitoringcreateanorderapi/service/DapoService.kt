package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Dapo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDapoDto
import java.util.UUID

@Service
class DapoService : OrderSectionServiceBase() {
  fun addDapo(orderId: UUID, username: String, dto: UpdateDapoDto): Dapo {
    val order = findEditableOrder(orderId, username)

    val match = order.dapoClauses.find { it.id == dto.id }
    order.dapoClauses.remove(match)

    val id = dto.id ?: UUID.randomUUID()
    order.dapoClauses.add(Dapo(id = id, versionId = order.versionId, clause = dto.clause, date = dto.date))

    orderRepo.save(order)

    return order.dapoClauses.find { it.id == id }!!
  }
}
