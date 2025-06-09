package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateCurfewAdditionalDetailsDto
import java.util.*

@Service
class CurfewAdditionalDetailsService : OrderSectionServiceBase() {
  fun updateCurfewAdditionalDetails(
    orderId: UUID,
    username: String,
    updateRecord: UpdateCurfewAdditionalDetailsDto,
  ): CurfewConditions {
    val order = findEditableOrder(orderId, username)

    order.curfewConditions = CurfewConditions(
      versionId = order.getCurrentVersion().id,
      curfewAddress = order.curfewConditions?.curfewAddress,
      endDate = order.curfewConditions?.endDate,
      startDate = order.curfewConditions?.startDate,
      curfewAdditionalDetails = updateRecord.curfewAdditionalDetails,
    )

    return orderRepo.save(order).curfewConditions!!
  }
}