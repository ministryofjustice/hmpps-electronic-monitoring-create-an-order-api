package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.PrisonerDetailsApi

@Service
class DeviceWearerDetailsService(private val webClient: PrisonerDetailsApi) {
  fun storeDetails(prisonNumber: String): StoreDetailsResponse = StoreDetailsResponse(success = true)
}

data class StoreDetailsResponse(val success: Boolean)
