package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceDto
import java.util.UUID

@Service
class OffenceService : OrderSectionServiceBase() {
  fun addOffence(orderId: UUID, username: String, dto: UpdateOffenceDto): Offence {
    val order = findEditableOrder(orderId, username)

    val match = order.offences.find { it.id == dto.id }
    order.offences.remove(match)

    val id = dto.id ?: UUID.randomUUID()
    order.offences.add(
      Offence(
        id = id,
        versionId = order.versionId,
        offenceType = dto.offenceType,
        offenceDate = dto.offenceDate,
      ),
    )

    orderRepo.save(order)

    return order.offences.find { it.id == id }!!
  }

  fun deleteOffence(orderId: UUID, username: String, offenceId: UUID) {
    val order = this.findEditableOrder(orderId, username)

    order.offences.removeIf { it.id == offenceId }

    orderRepo.save(order)
  }
}
