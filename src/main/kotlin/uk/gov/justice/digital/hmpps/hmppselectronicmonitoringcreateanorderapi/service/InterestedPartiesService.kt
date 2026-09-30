package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInterestedPartiesDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import java.util.*

@Service
class InterestedPartiesService(private val addressService: AddressService) : OrderSectionServiceBase() {
  fun updateInterestedParties(
    orderId: UUID,
    username: String,
    updateRecord: UpdateInterestedPartiesDto,
  ): InterestedParties {
    val order = this.findEditableOrder(orderId, username)
    val existingNotifyingOrganisation = order.interestedParties?.notifyingOrganisation

    val newInterestedParties = InterestedParties(
      versionId = order.getCurrentVersion().id,
      notifyingOrganisation = updateRecord.notifyingOrganisation.toString(),
      notifyingOrganisationName = updateRecord.notifyingOrganisationName,
      notifyingOrganisationEmail = updateRecord.notifyingOrganisationEmail,
      responsibleOfficerName = updateRecord.responsibleOfficerName,
      responsibleOfficerPhoneNumber = updateRecord.responsibleOfficerPhoneNumber,
      responsibleOrganisation = updateRecord.responsibleOrganisation?.toString(),
      responsibleOrganisationRegion = updateRecord.responsibleOrganisationRegion,
      responsibleOrganisationEmail = updateRecord.responsibleOrganisationEmail,
      responsibleOfficerFirstName = updateRecord.responsibleOfficerFirstName,
      responsibleOfficerLastName = updateRecord.responsibleOfficerLastName,
      responsibleOfficerEmail = updateRecord.responsibleOfficerEmail,
    )

    if (newInterestedParties.responsibleOrganisation != order.interestedParties?.responsibleOrganisation ||
      newInterestedParties.responsibleOrganisationRegion != order.interestedParties?.responsibleOrganisationRegion
    ) {
      order.probationDeliveryUnit = null
    }

    if (order.getCurrentVersion().versionId == 0) {
      if (existingNotifyingOrganisation == null && order.isSentencingAct == null) {
        order.isSentencingAct = updateRecord.notifyingOrganisation in listOf(
          NotifyingOrganisationDDv5.PRISON,
          NotifyingOrganisationDDv5.PROBATION,
        )
      } else if (existingNotifyingOrganisation != null &&
        existingNotifyingOrganisation != newInterestedParties.notifyingOrganisation
      ) {
        order.isSentencingAct = null
      }
    }
    order.interestedParties = newInterestedParties

    return updateLastUpdatedByAndSaveOrder(order, interestedParties = newInterestedParties).interestedParties!!
  }
}
