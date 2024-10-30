package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateAddressDto
import java.util.UUID

@Service
class AddressService() : OrderSectionServiceBase() {
  @Transactional
  fun updateAddress(orderId: UUID, username: String, updateRecord: UpdateAddressDto): Address? {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    // If the address type exists already, update it - otherwise, create it
    val existingAddress = order.addresses.find { it.addressType == updateRecord.addressType }
    var newAddress: Address? = null
    if (existingAddress != null) {
      existingAddress.addressLine1 = updateRecord.addressLine1
      existingAddress.addressLine2 = updateRecord.addressLine2
      existingAddress.addressLine3 = updateRecord.addressLine3
      existingAddress.addressLine4 = updateRecord.addressLine4
      existingAddress.postcode = updateRecord.postcode
    } else {
      newAddress = Address(
        orderId = orderId,
        addressType = updateRecord.addressType,
        addressLine1 = updateRecord.addressLine1,
        addressLine2 = updateRecord.addressLine2,
        addressLine3 = updateRecord.addressLine3,
        addressLine4 = updateRecord.addressLine4,
        postcode = updateRecord.postcode,
      )
      order.addresses.add(newAddress)
    }
    orderRepo.save(order)
    return existingAddress ?: newAddress
  }
}
