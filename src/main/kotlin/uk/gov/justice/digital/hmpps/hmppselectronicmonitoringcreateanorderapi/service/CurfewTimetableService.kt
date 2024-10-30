package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import java.util.*

@Service
class CurfewTimetableService : OrderSectionServiceBase() {
  fun updateCurfewTimetable(orderId: UUID, username: String, timetable: List<CurfewTimeTable>) {
    val order = findEditableOrder(orderId, username)
    order.curfewTimeTable.clear()
    order.curfewTimeTable.addAll(timetable)
    orderRepo.save(order)
  }
}
