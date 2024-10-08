package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerAddressRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateDeviceWearerAddressDto
import java.util.UUID

@Service
class DeviceWearerAddressService(
  val repo: DeviceWearerAddressRepository,
) {
  fun createOrUpdateAddress(
    orderId: UUID,
    username: String,
    deviceWearerAddressUpdateRecord: UpdateDeviceWearerAddressDto,
  ): DeviceWearerAddress {
    // BAD LOGIC - will allow creation of address against submitted order or another user's order
    val address = repo.findByOrderIdAndOrderUsernameAndOrderStatusAndAddressType(
      orderId,
      username,
      FormStatus.IN_PROGRESS,
      deviceWearerAddressUpdateRecord.addressType,
    ).orElse(
      DeviceWearerAddress(
        orderId = orderId,
        addressType = deviceWearerAddressUpdateRecord.addressType,
      ),
    )

    with(deviceWearerAddressUpdateRecord) {
      address.addressLine1 = addressLine1
      address.addressLine2 = addressLine2
      address.addressLine3 = addressLine3
      address.addressLine4 = addressLine4
      address.postcode = postCode
    }

    return repo.save(address)
  }
}
