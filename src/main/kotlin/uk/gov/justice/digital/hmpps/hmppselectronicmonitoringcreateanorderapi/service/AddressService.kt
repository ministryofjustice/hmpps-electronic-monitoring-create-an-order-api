package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateDeviceWearerAddressDto
import java.util.UUID

@Service
class AddressService(
  val orderRepo: OrderRepository,
  val addressRepo: AddressRepository,
) {

  fun deleteAddress(
    orderId: UUID,
    username: String,
    addressType: AddressType,
  ) {
    addressRepo.deleteByOrderIdAndOrderUsernameAndAddressType(
      orderId,
      username,
      addressType,
    )
  }

  fun updateAddress(
    orderId: UUID,
    username: String,
    updateRecord: UpdateDeviceWearerAddressDto,
  ): Address {
    // Verify the order belongs to the user and is in draft state
    orderRepo.findByIdAndUsernameAndStatus(
      orderId,
      username,
      OrderStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("An editable order with $orderId does not exist")
    }

    // Remove the existing address
    this.deleteAddress(
      orderId,
      username,
      updateRecord.addressType,
    )

    // Create a new address
    val address = Address(
      orderId = orderId,
      addressType = updateRecord.addressType,
      addressLine1 = updateRecord.addressLine1,
      addressLine2 = updateRecord.addressLine2,
      addressLine3 = updateRecord.addressLine3,
      addressLine4 = updateRecord.addressLine4,
      postcode = updateRecord.postcode,
    )

    return addressRepo.save(address)
  }
}
