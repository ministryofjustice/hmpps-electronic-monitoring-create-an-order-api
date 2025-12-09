package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.FormValidationException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInterestedPartiesDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FamilyCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthCustodyServiceRegionDDv5
import java.util.*

@Service
class InterestedPartiesService(private val addressService: AddressService) : OrderSectionServiceBase() {
  fun updateInterestedParties(
    orderId: UUID,
    username: String,
    updateRecord: UpdateInterestedPartiesDto,
  ): InterestedParties {
    val order = this.findEditableOrder(orderId, username)

    if (order.dataDictionaryVersion != DataDictionaryVersion.DDV4) {
      val invalidNotifyingOrganisation =
        when (updateRecord.notifyingOrganisation) {
          NotifyingOrganisationDDv5.FAMILY_COURT ->
            FamilyCourtDDv5.entries.none { it.name == updateRecord.notifyingOrganisationName }
          NotifyingOrganisationDDv5.PROBATION ->
            updateRecord.notifyingOrganisationName != "" &&
              ProbationServiceRegion.entries.none { it.name == updateRecord.notifyingOrganisationName }
          NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE ->
            updateRecord.notifyingOrganisationName != "" &&
              YouthCustodyServiceRegionDDv5.entries.none { it.name == updateRecord.notifyingOrganisationName }
          else -> false
        }

      if (invalidNotifyingOrganisation) {
        throw FormValidationException(
          "notifyingOrganisationName",
          ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_NAME_REQUIRED,
        )
      }
    }

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

    if (newInterestedParties.responsibleOrganisation != order.interestedParties?.responsibleOrganisation ||
      newInterestedParties.responsibleOrganisationRegion != order.interestedParties?.responsibleOrganisationRegion
    ) {
      order.probationDeliveryUnit = null
    }

    order.interestedParties = newInterestedParties

    return orderRepo.save(order).interestedParties!!
  }
}
