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

    order.offences.add(
      Offence(
        versionId = order.versionId,
        offenceType = dto.offenceType,
        offenceDate = dto.offenceDate,
      ),
    )

    orderRepo.save(order)

    // TODO: Not the best way to do this
    return order.offences.find { it.offenceType == dto.offenceType }!!
  }
}
