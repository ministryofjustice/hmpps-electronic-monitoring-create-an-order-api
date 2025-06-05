package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInterestedPartiesDto
import java.util.*

@Service
class InterestedPartiesService(private val addressService: AddressService) : OrderSectionServiceBase() {
  fun updateInterestedParties(
    orderId: UUID,
    username: String,
    updateRecord: UpdateInterestedPartiesDto,
  ): InterestedParties {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    val newInterestedParties = InterestedParties(
      versionId = order.getCurrentVersion().id,
      notifyingOrganisation = updateRecord.notifyingOrganisation.toString(),
      notifyingOrganisationName = updateRecord.notifyingOrganisationName,
      notifyingOrganisationEmail = updateRecord.notifyingOrganisationEmail,
      responsibleOfficerName = updateRecord.responsibleOfficerName,
      responsibleOfficerPhoneNumber = updateRecord.responsibleOfficerPhoneNumber,
      responsibleOrganisation = updateRecord.responsibleOrganisation.toString(),
      responsibleOrganisationRegion = updateRecord.responsibleOrganisationRegion,
      responsibleOrganisationEmail = updateRecord.responsibleOrganisationEmail,
    )
    // clear previously selected probation delivery unit if responsible organisation or responsible organisation region changed
    if (newInterestedParties.responsibleOrganisation != order.interestedParties?.responsibleOfficerName ||
      newInterestedParties.responsibleOrganisationRegion != order.interestedParties?.responsibleOrganisationRegion
    ) {
      order.probationDeliveryUnit = null
    }
    order.interestedParties = newInterestedParties
    return orderRepo.save(order).interestedParties!!
  }
}
