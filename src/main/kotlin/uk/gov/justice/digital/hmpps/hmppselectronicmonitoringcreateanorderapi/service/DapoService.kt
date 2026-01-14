package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Dapo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDapoDto
import java.util.UUID

@Service
class DapoService : OrderSectionServiceBase() {
  fun addDapo(orderId: UUID, username: String, dto: UpdateDapoDto): Dapo {
    val order = findEditableOrder(orderId, username)

    order.dapoClauses.add(Dapo(versionId = order.versionId, clause = dto.clause, date = dto.date))

    orderRepo.save(order)

    // TODO: Not the best way to do this
    return order.dapoClauses.find { it.clause == dto.clause }!!
  }
}
