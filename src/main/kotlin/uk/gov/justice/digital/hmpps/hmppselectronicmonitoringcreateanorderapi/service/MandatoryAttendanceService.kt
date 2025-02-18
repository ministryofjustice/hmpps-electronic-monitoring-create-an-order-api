package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MandatoryAttendanceConditions
import java.util.*

@Service
class MandatoryAttendanceService : OrderSectionServiceBase() {
  fun updateMandatoryAttendance(
    orderId: UUID,
    username: String,
    record: MandatoryAttendanceConditions,
  ): MandatoryAttendanceConditions {
    val order = this.findEditableOrder(orderId, username)

    if (order.mandatoryAttendanceConditions.isEmpty()) {
      order.mandatoryAttendanceConditions.add(record)
    } else {
      val appointment = order.mandatoryAttendanceConditions.find { x -> x.id == record.id }

      order.mandatoryAttendanceConditions.remove(appointment)
      order.mandatoryAttendanceConditions.add(record)
    }

    val saved = this.orderRepo.save(order)

    return saved.mandatoryAttendanceConditions.find { x -> x.id == record.id }!!
  }
}
