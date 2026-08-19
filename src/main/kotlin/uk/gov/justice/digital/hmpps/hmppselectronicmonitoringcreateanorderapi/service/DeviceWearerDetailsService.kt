package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.PrisonerDetailsApi
import java.time.ZonedDateTime

@Service
class DeviceWearerDetailsService(private val webClient: PrisonerDetailsApi) {

  fun storeDetails(prisonNumber: String): StoreDetailsResponse = StoreDetailsResponse(success = true)

  fun getDetailsOverview(prisonNumber: String): GetDetailsResponse {
    val details = webClient.getPrisonerDetails(prisonNumber)

    return GetDetailsResponse(
      firstName = details.firstName,
      lastName = details.lastName,
      dateOfBirth = details.parsedDateOfBirth(),
      prisonNumber = prisonNumber,
    )
  }
}

data class StoreDetailsResponse(val success: Boolean)

data class GetDetailsResponse(
  val firstName: String?,
  val lastName: String?,
  val dateOfBirth: ZonedDateTime?,
  val prisonNumber: String,
)
