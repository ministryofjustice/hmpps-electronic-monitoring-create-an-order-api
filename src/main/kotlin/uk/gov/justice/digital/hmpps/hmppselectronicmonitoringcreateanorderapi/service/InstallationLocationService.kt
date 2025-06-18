package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationLocationDto
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
    return orderRepo.save(order).installationLocation!!
  }
}
