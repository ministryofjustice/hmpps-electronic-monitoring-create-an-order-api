package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.PrisonerDetailsApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Result
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.GetPrisonDetailsResponse
import java.util.UUID

@Service
class DeviceWearerDetailsService(private val webClient: PrisonerDetailsApi) : OrderSectionServiceBase() {

  fun storeDetails(prisonNumber: String, orderId: UUID, username: String): Result<Unit> {
    try {
      val order = this.findEditableOrder(orderId, username)
      val record = webClient.getPrisonerDetails(prisonNumber, order.versionId)

      order.deviceWearer = record.deviceWearer
      order.addresses.addAll(record.addresses)
      order.contactDetails = record.contactDetails

      orderRepo.save(order)

      return Result(success = true)
    } catch (error: Exception) {
      return Result(success = false, error = error)
    }
  }

  fun getDetailsOverview(prisonNumber: String): GetPrisonDetailsResponse {
    val details = webClient.getPrisonerDetails(prisonNumber, UUID.randomUUID())

    return GetPrisonDetailsResponse(
      firstName = details.deviceWearer?.firstName,
      lastName = details.deviceWearer?.lastName,
      dateOfBirth = details.deviceWearer?.dateOfBirth,
      prisonNumber = prisonNumber,
    )
  }
}
