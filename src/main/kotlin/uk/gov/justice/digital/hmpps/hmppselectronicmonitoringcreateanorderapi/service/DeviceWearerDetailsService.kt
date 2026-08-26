package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.CorePersonRecordApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.GetCorePersonDetailsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import java.util.UUID

@Service
class DeviceWearerDetailsService(private val webClient: CorePersonRecordApi) : OrderSectionServiceBase() {

  fun storeDetails(organisationSearchId: String, orderId: UUID, username: String) {
    val normalisedOrganisationSearchId = normaliseOrganisationSearchId(organisationSearchId)

    val order = this.findEditableOrder(orderId, username)
    val notifyingOrganisation = order.interestedParties?.notifyingOrganisation
    val record = fetchPersonRecord(notifyingOrganisation, normalisedOrganisationSearchId, order.versionId)
    order.deviceWearer = record.deviceWearer
    order.addresses.addAll(record.addresses)
    order.contactDetails = record.contactDetails

    orderRepo.save(order)
  }

  fun getDetailsOverview(organisationSearchId: String, orderId: UUID, username: String): GetCorePersonDetailsResponse {
    val normalisedOrganisationSearchId = normaliseOrganisationSearchId(organisationSearchId)
    val order = this.findEditableOrder(orderId, username)
    val notifyingOrganisation = order.interestedParties?.notifyingOrganisation
    val details = fetchPersonRecord(notifyingOrganisation, normalisedOrganisationSearchId, order.versionId)

    return GetCorePersonDetailsResponse(
      firstName = details.deviceWearer?.firstName,
      lastName = details.deviceWearer?.lastName,
      dateOfBirth = details.deviceWearer?.dateOfBirth,
      organisationSearchId = normalisedOrganisationSearchId,
    )
  }

  private fun fetchPersonRecord(notifyingOrganisation: String?, organisationSearchId: String, versionId: UUID) = when {
    notifyingOrganisation == NotifyingOrganisationDDv5.PRISON.name ->
      webClient.getPersonByPrisonNumber(organisationSearchId, versionId)
    notifyingOrganisation == NotifyingOrganisationDDv5.PROBATION.name ->
      webClient.getPersonByCrn(organisationSearchId, versionId)
    else -> throw BadRequestException(
      "Notifying organisation $notifyingOrganisation is unsupported for Core Person Record lookup",
    )
  }

  private fun normaliseOrganisationSearchId(organisationSearchId: String): String {
    val trimmedOrganisationSearchId = organisationSearchId.trim()
    if (trimmedOrganisationSearchId.isBlank()) {
      throw BadRequestException("organisationSearchId must not be blank")
    }

    return trimmedOrganisationSearchId
  }
}
