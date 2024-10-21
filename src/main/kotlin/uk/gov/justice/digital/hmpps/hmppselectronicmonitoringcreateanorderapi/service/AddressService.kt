package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateDeviceWearerAddressDto
import java.util.UUID

@Service
class AddressService(

  val addressRepo: AddressRepository,
) : OrderSectionServiceBase() {

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

  @Transactional
  fun updateAddress(
    orderId: UUID,
    username: String,
    updateRecord: UpdateDeviceWearerAddressDto,
  ): Address {
    // Verify the order belongs to the user and is in draft state
    this.findEditableOrder(orderId, username)
    // Remove the existing address
    this.deleteAddress(
      orderId,
      username,
      updateRecord.addressType,
    )

    addressRepo.flush()

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
