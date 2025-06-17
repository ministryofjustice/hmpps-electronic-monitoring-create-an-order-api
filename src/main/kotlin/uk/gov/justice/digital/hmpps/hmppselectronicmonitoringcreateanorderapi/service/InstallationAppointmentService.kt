package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationAppointmentDto
import java.util.*

@Service
class InstallationAppointmentService : OrderSectionServiceBase() {
  fun createOrUpdateInstallationAppointment(
    orderId: UUID,
    username: String,
    updateRecord: UpdateInstallationAppointmentDto,
  ): InstallationAppointment {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)
    order.installationAppointment = InstallationAppointment(
      versionId = order.getCurrentVersion().id,
      placeName = updateRecord.placeName,
      appointmentDate = updateRecord.appointmentDate,
    )
    return orderRepo.save(order).installationAppointment!!
  }
}
