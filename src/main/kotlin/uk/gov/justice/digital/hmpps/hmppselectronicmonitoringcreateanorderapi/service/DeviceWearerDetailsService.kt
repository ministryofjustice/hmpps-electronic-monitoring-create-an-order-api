package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.CorePersonRecordApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.CreateAndVaryALicenceApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.GetCorePersonDetailsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence.LicenceConditionMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence.LicenceSummary
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.interfaces.MonitoringCondition
import java.util.*

@Service
class DeviceWearerDetailsService(
  private val webClient: CorePersonRecordApi,
  private val createAndVaryALicenceApi: CreateAndVaryALicenceApi,
  private val licenceConditionMapper: LicenceConditionMapper,
  private val monitoringConditionsService: MonitoringConditionsService,
  private val featureFlags: FeatureFlags,
) : OrderSectionServiceBase() {

  fun storeDetails(organisationSearchId: String, orderId: UUID, username: String) {
    val normalisedOrganisationSearchId = normaliseOrganisationSearchId(organisationSearchId)

    val order = this.findEditableOrder(orderId, username)
    val notifyingOrganisation = order.interestedParties?.notifyingOrganisation
    val record = fetchPersonRecord(notifyingOrganisation, normalisedOrganisationSearchId, order.versionId)
    order.deviceWearer = record.deviceWearer
    order.addresses.addAll(record.addresses)
    order.contactDetails = record.contactDetails

    // TODO: Experimenting for the cvl integration, possibly can add a call here to get all licences based on the normalisedOrganisationSearchId
    // Can then add that to the GetCorePersonDetailsResponse below.
    if (featureFlags.cvlIntegrationEnabled) {
      val licenceSummary = fetchCvlRecord(notifyingOrganisation, normalisedOrganisationSearchId)
      val licence = licenceSummary?.let { createAndVaryALicenceApi.getLicenceConditions(it.id) }
      if (licence != null) {
        val monitoringConditions: List<MonitoringCondition> = licenceConditionMapper.map(licence, order.versionId)
        monitoringConditionsService.applyImportedConditions(order, monitoringConditions)
      }
    }

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
    notifyingOrganisation == NotifyingOrganisationDDv5.PRISON.name ||
      notifyingOrganisation == NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE.name ->
      webClient.getPersonByPrisonNumber(organisationSearchId, versionId)
    notifyingOrganisation == NotifyingOrganisationDDv5.PROBATION.name ->
      webClient.getPersonByCrn(organisationSearchId, versionId)
    else -> throw BadRequestException(
      "Notifying organisation $notifyingOrganisation is unsupported for Core Person Record lookup",
    )
  }

  private fun fetchCvlRecord(notifyingOrganisation: String?, organisationSearchId: String): LicenceSummary? = when {
    notifyingOrganisation == NotifyingOrganisationDDv5.PRISON.name ||
      notifyingOrganisation == NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE.name ->
      createAndVaryALicenceApi.getLicenceByPrisonNumber(organisationSearchId)
    notifyingOrganisation == NotifyingOrganisationDDv5.PROBATION.name ->
      createAndVaryALicenceApi.getLicenceByCrn(organisationSearchId)
    else -> throw BadRequestException(
      "Notifying organisation $notifyingOrganisation is unsupported for CVL record lookup",
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
