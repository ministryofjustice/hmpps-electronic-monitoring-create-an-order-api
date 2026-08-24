package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.CorePersonRecordApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordAuthorisationException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordDependencyException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Result
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.GetCorePersonDetailsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import java.util.UUID

@Service
class DeviceWearerDetailsService(private val webClient: CorePersonRecordApi) : OrderSectionServiceBase() {

  fun storeDetails(organisationSearchId: String, orderId: UUID, username: String): Result<Unit> {
    val normalisedOrganisationSearchId = normaliseOrganisationSearchId(organisationSearchId)

    return try {
      val order = this.findEditableOrder(orderId, username)
      val notifyingOrganisation = order.interestedParties?.notifyingOrganisation
      val record = fetchPersonRecord(notifyingOrganisation, normalisedOrganisationSearchId, order.versionId)
      order.deviceWearer = record.deviceWearer
      order.addresses.addAll(record.addresses)
      order.contactDetails = record.contactDetails

      orderRepo.save(order)
      Result(success = true)
    } catch (error: EntityNotFoundException) {
      Result(success = false, error = error)
    } catch (error: WebClientResponseException.NotFound) {
      Result(
        success = false,
        error = EntityNotFoundException(
          "No wearer details were found for id $normalisedOrganisationSearchId",
        ),
      )
    } catch (error: WebClientResponseException.Forbidden) {
      Result(
        success = false,
        error = CorePersonRecordAuthorisationException(
          "Core Person Record authorisation failed for id $normalisedOrganisationSearchId",
          error,
        ),
      )
    } catch (error: WebClientResponseException) {
      Result(
        success = false,
        error = CorePersonRecordDependencyException(
          "Core Person Record lookup failed for id $normalisedOrganisationSearchId",
          error.statusCode,
          error,
        ),
      )
    } catch (error: WebClientRequestException) {
      Result(
        success = false,
        error = CorePersonRecordDependencyException(
          "Core Person Record is unavailable for id $normalisedOrganisationSearchId",
          null,
          error,
        ),
      )
    }
  }

  fun getDetailsOverview(organisationSearchId: String, notifyingOrganisation: String?): GetCorePersonDetailsResponse {
    val normalisedOrganisationSearchId = normaliseOrganisationSearchId(organisationSearchId)
    val details = fetchPersonRecord(notifyingOrganisation, normalisedOrganisationSearchId, UUID.randomUUID())

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
