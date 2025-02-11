package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateCurfewTimetableDto
import java.util.*

@Service
class CurfewTimetableService : OrderSectionServiceBase() {
  fun updateCurfewTimetable(
    orderId: UUID,
    username: String,
    updateRecord: List<UpdateCurfewTimetableDto>,
  ): List<CurfewTimeTable> {
    val order = findEditableOrder(orderId, username)

    order.curfewTimeTable.clear()
    order.curfewTimeTable.addAll(
      updateRecord.map {
        CurfewTimeTable(
          versionId = order.getCurrentVersion().id,
          curfewAddress = it.curfewAddress,
          dayOfWeek = it.dayOfWeek,
          endTime = it.endTime,
          startTime = it.startTime,
        )
      },
    )

    return orderRepo.save(order).curfewTimeTable
  }
}
