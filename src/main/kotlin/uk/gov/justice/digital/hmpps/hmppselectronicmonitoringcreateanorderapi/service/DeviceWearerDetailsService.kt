package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.PrisonerDetailsApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Result
import java.time.ZonedDateTime
import java.util.UUID

@Service
class DeviceWearerDetailsService(private val webClient: PrisonerDetailsApi) : OrderSectionServiceBase() {

  fun storeDetails(prisonNumber: String, orderId: UUID, username: String): Result<Unit> {
    try {
      val order = this.findEditableOrder(orderId, username)
      val details = webClient.getPrisonerDetails(prisonNumber)

      order.deviceWearer = details.toDeviceWearer(order.versionId)
      orderRepo.save(order)

      return Result(success = true)
    } catch (error: Exception) {
      return Result(success = false, error = error)
    }
  }

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

data class GetDetailsResponse(
  val firstName: String?,
  val lastName: String?,
  val dateOfBirth: ZonedDateTime?,
  val prisonNumber: String,
)
