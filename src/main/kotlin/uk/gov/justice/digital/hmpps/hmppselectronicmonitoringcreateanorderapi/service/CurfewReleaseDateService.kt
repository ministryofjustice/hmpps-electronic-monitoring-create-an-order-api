package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateCurfewReleaseDateConditionsDto
import java.util.*

@Service
class CurfewReleaseDateService : OrderSectionServiceBase() {
  fun updateCurfewReleaseDateCondition(
    orderId: UUID,
    username: String,
    updateRecord: UpdateCurfewReleaseDateConditionsDto,
  ): CurfewReleaseDateConditions {
    val order = findEditableOrder(orderId, username)

    order.curfewReleaseDateConditions = CurfewReleaseDateConditions(
      versionId = order.getCurrentVersion().id,
      curfewAddress = updateRecord.curfewAddress,
      endTime = updateRecord.endTime,
      releaseDate = updateRecord.releaseDate,
      startTime = updateRecord.startTime,
    )

    return orderRepo.save(order).curfewReleaseDateConditions!!
  }
}
