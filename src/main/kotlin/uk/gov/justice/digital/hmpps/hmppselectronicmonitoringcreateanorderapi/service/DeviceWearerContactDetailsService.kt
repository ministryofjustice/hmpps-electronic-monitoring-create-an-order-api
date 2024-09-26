package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerContactDetailsRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateContactDetailsDto
import java.util.*

@Service
class DeviceWearerContactDetailsService(
  val repo: DeviceWearerContactDetailsRepository,
) {

  fun getContactDetails(orderId: UUID, username: String): DeviceWearerContactDetails {
    return repo.findByOrderIdAndOrderUsername(orderId, username).orElseThrow { EntityNotFoundException("Contact Details for $orderId not found") }
  }

  fun updateContactDetails(orderId: UUID, username: String, updateContactDetailsRecord: UpdateContactDetailsDto): DeviceWearerContactDetails {
    val contactDetails = repo.findByOrderIdAndOrderUsername(orderId, username).orElseThrow { EntityNotFoundException("Contact Details for $orderId not found") }

    with(updateContactDetailsRecord) {
      contactDetails.contactNumber = contactNumber
    }

    return repo.save(contactDetails)
  }
}
