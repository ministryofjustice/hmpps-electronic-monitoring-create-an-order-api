package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateAddressDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInterestedPartiesDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.util.*

@Service
class InterestedPartiesService(
  private val addressService: AddressService,
) : OrderSectionServiceBase() {
  fun updateInterestedParties(
    orderId: UUID,
    username: String,
    updateRecord: UpdateInterestedPartiesDto,
  ): InterestedParties {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    val address = addressService.updateAddress(
      orderId,
      username,
      UpdateAddressDto(
        addressType = AddressType.RESPONSIBLE_ORGANISATION,
        addressLine1 = updateRecord.responsibleOrganisationAddressLine1,
        addressLine2 = updateRecord.responsibleOrganisationAddressLine2,
        addressLine3 = updateRecord.responsibleOrganisationAddressLine3,
        addressLine4 = updateRecord.responsibleOrganisationAddressLine4,
        postcode = updateRecord.responsibleOrganisationAddressPostcode,
      ),
    )

    val interestedParties = InterestedParties(
      orderId = order.id,
      notifyingOrganisation = "",
      notifyingOrganisationEmail = updateRecord.notifyingOrganisationEmail,
      responsibleOfficerName = updateRecord.responsibleOfficerName,
      responsibleOfficerPhoneNumber = updateRecord.responsibleOfficerPhoneNumber,
      responsibleOrganisation = updateRecord.responsibleOrganisation.toString(),
      responsibleOrganisationRegion = updateRecord.responsibleOrganisationRegion,
      responsibleOrganisationPhoneNumber = updateRecord.responsibleOrganisationPhoneNumber,
      responsibleOrganisationEmail = updateRecord.responsibleOrganisationEmail,
      responsibleOrganisationAddress = address,
    )

    order.interestedParties = interestedParties
    orderRepo.save(order)

    return interestedParties
  }
}
