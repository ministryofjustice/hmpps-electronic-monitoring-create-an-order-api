package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateContactDetailsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.ContactDetailsRepository
import java.util.*

@Service
class ContactDetailsService(
  val repo: ContactDetailsRepository,
) {

  fun getContactDetails(orderId: UUID, username: String): ContactDetails {
    return repo.findByOrderIdAndOrderUsername(
      orderId,
      username,
    ).orElseThrow {
      EntityNotFoundException("Contact Details for $orderId not found")
    }
  }

  fun updateContactDetails(
    orderId: UUID,
    username: String,
    updateContactDetailsRecord: UpdateContactDetailsDto,
  ): ContactDetails {
    val contactDetails = repo.findByOrderIdAndOrderUsernameAndOrderStatus(
      orderId,
      username,
      OrderStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("Contact Details for $orderId not found")
    }

    with(updateContactDetailsRecord) {
      contactDetails.contactNumber = contactNumber
    }

    return repo.save(contactDetails)
  }
}
