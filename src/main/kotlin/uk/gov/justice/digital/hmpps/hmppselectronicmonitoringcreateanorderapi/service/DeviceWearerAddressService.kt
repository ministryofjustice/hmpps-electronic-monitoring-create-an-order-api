package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
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

    // Verify that the primary order isn't marked as the installation order
    if (addressType === DeviceWearerAddressType.INSTALLATION) {
      val primaryAddress = addressRepo.findByOrderIdAndOrderUsernameAndOrderStatusAndAddressType(
        order.id,
        order.username,
        order.status,
        DeviceWearerAddressType.PRIMARY,
      ).orElse(null)

      if (primaryAddress != null && primaryAddress.installationAddress) {
        throw ValidationException(
          "An installation address already exists for Order: ${order.id}",
        )
      }
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
        address = Address(),
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
      if (!noFixedAbode) {
        address.address.addressLine1 = addressLine1
        address.address.addressLine2 = addressLine2
        address.address.addressLine3 = addressLine3
        address.address.addressLine4 = addressLine4
        address.address.postcode = postcode
      }
      address.noFixedAbode = noFixedAbode
      address.installationAddress = installationAddress
    }

    return addressRepo.save(address)
  }
}
