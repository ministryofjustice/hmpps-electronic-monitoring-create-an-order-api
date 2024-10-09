package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerAddressRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateDeviceWearerAddressDto
import java.util.UUID

@Service
class DeviceWearerAddressService(
  val orderRepo: OrderFormRepository,
  val addressRepo: DeviceWearerAddressRepository,
) {
  fun getAddress(
    orderId: UUID,
    username: String,
    addressType: DeviceWearerAddressType,
  ): DeviceWearerAddress {
    // Verify the order belongs to the user and is in draft state
    val order = orderRepo.findByIdAndUsernameAndStatus(
      orderId,
      username,
      FormStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("An editable order with $orderId does not exist")
    }

    // Find an existing address or create a new address
    return addressRepo.findByOrderIdAndOrderUsernameAndOrderStatusAndAddressType(
      order.id,
      order.username,
      order.status,
      addressType,
    ).orElse(
      DeviceWearerAddress(
        orderId = orderId,
        addressType = addressType,
      ),
    )
  }

  fun createOrUpdateAddress(
    orderId: UUID,
    username: String,
    deviceWearerAddressUpdateRecord: UpdateDeviceWearerAddressDto,
  ): DeviceWearerAddress {
    val address = this.getAddress(
      orderId,
      username,
      deviceWearerAddressUpdateRecord.addressType,
    )

    with(deviceWearerAddressUpdateRecord) {
      address.addressLine1 = addressLine1
      address.addressLine2 = addressLine2
      address.addressLine3 = addressLine3
      address.addressLine4 = addressLine4
      address.postcode = postcode
    }

    return addressRepo.save(address)
  }
}
