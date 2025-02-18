package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MandatoryAttendanceConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMandatoryAttendanceDto
import java.util.*

@Service
class MandatoryAttendanceService : OrderSectionServiceBase() {
  fun updateMandatoryAttendance(
    orderId: UUID,
    username: String,
    updateRecord: UpdateMandatoryAttendanceDto,
  ): MandatoryAttendanceConditions {
    val order = this.findEditableOrder(orderId, username)
    val conditions = MandatoryAttendanceConditions(
      id = updateRecord.id ?: UUID.randomUUID(),
      versionId = order.getCurrentVersion().id,
      appointmentDay = updateRecord.appointmentDay,
      endDate = updateRecord.endDate,
      endTime = updateRecord.endTime,
      purpose = updateRecord.purpose,
      startDate = updateRecord.startDate,
      startTime = updateRecord.startTime,
      addressLine1 = updateRecord.addressLine1,
      addressLine2 = updateRecord.addressLine2,
      addressLine3 = updateRecord.addressLine3,
      addressLine4 = updateRecord.addressLine4,
      postcode = updateRecord.postcode,
    )

    if (order.mandatoryAttendanceConditions.isEmpty()) {
      order.mandatoryAttendanceConditions.add(conditions)
    } else {
      val appointment = order.mandatoryAttendanceConditions.find { x -> x.id == conditions.id }

      order.mandatoryAttendanceConditions.remove(appointment)
      order.mandatoryAttendanceConditions.add(conditions)
    }

    val saved = this.orderRepo.save(order)

    return saved.mandatoryAttendanceConditions.find { x -> x.id == conditions.id }!!
  }
}
