package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateContactDetailsDto
import java.util.*

@Service
class ContactDetailsService : OrderSectionServiceBase() {
  fun updateContactDetails(
    orderId: UUID,
    username: String,
    updateContactDetailsRecord: UpdateContactDetailsDto,
  ): ContactDetails {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    order.contactDetails = ContactDetails(
      versionId = order.getCurrentVersion().id,
      contactNumber = updateContactDetailsRecord.contactNumber,
      phoneNumberAvailable = updateContactDetailsRecord.phoneNumberAvailable,
    )

    return orderRepo.save(order).contactDetails!!
  }
}
