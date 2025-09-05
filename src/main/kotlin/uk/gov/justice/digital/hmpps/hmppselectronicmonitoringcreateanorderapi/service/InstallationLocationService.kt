package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationLocationDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import java.util.*

@Service
class InstallationLocationService : OrderSectionServiceBase() {

  fun createOrUpdateInstallationLocation(
    orderId: UUID,
    username: String,
    updateRecord: UpdateInstallationLocationDto,
  ): InstallationLocation {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)
    order.installationLocation = InstallationLocation(
      versionId = order.getCurrentVersion().id,
      location = updateRecord.location,
    )

    if (
      updateRecord.location == InstallationLocationType.PRIMARY ||
      updateRecord.location == InstallationLocationType.SECONDARY ||
      updateRecord.location == InstallationLocationType.TERTIARY
    ) {
      order.installationAppointment = null
      order.addresses.removeAll { it.addressType == AddressType.INSTALLATION }
    }
    return orderRepo.save(order).installationLocation!!
  }
}
