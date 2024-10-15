package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerAddressRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateDeviceWearerAddressDto
import java.util.UUID

@Service
class DeviceWearerAddressService(
  val orderRepo: OrderRepository,
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
      OrderStatus.IN_PROGRESS,
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

    if (deviceWearerAddressUpdateRecord.addressType !== DeviceWearerAddressType.NO_FIXED_ABODE) {
      with(deviceWearerAddressUpdateRecord) {
        address.address = Address(
          addressLine1 = addressLine1,
          addressLine2 = addressLine2,
          addressLine3 = addressLine3,
          addressLine4 = addressLine4,
          postcode = postcode,
        )
        address.installationAddress = installationAddress
      }
    }

    return addressRepo.save(address)
  }
}
