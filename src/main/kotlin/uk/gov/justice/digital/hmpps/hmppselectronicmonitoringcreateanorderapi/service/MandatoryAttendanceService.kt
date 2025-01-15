package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MandatoryAttendanceConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MandatoryAttendanceRepository
import java.util.*

@Service
class MandatoryAttendanceService(
  val repo: MandatoryAttendanceRepository
) {
  fun updateMandatoryAttendance(
    orderId: UUID,
    username: String,
    ): MandatoryAttendanceConditions {
    val mandatoryAttendance = repo.findByOrderIdAndOrderUsernameAndOrderStatus(
      orderId,
      username,
      OrderStatus.IN_PROGRESS
    ).orElseThrow {
      EntityNotFoundException("No editable Mandatory Attendance record was found for for Order: $orderId")
    }

    with(mandatoryAttendance) {
      mandatoryAttendance.startDate = startDate
      mandatoryAttendance.endDate = endDate
      mandatoryAttendance.purpose = purpose
      mandatoryAttendance.appointmentDay = appointmentDay
      mandatoryAttendance.startTime = startTime
      mandatoryAttendance.endTime = endTime
      mandatoryAttendance.addressLine1 = addressLine1
      mandatoryAttendance.addressLine2 = addressLine2
      mandatoryAttendance.addressLine3 = addressLine3
      mandatoryAttendance.addressLine4 = addressLine4
      mandatoryAttendance.postcode = postcode

      return repo.save(mandatoryAttendance)
    }
  }
}