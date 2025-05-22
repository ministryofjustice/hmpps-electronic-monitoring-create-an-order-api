package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateCurfewConditionsDto
import java.util.*

@Service
class CurfewConditionService : OrderSectionServiceBase() {
  fun updateCurfewCondition(
    orderId: UUID,
    username: String,
    updateRecord: UpdateCurfewConditionsDto,
  ): CurfewConditions {
    val order = findEditableOrder(orderId, username)

    order.curfewConditions = CurfewConditions(
      versionId = order.getCurrentVersion().id,
      curfewAddress = updateRecord.curfewAddress,
      endDate = getDefaultZonedDateTime(updateRecord.endDate, 23, 59),
      startDate = updateRecord.startDate,
    )

    return orderRepo.save(order).curfewConditions!!
  }
}
