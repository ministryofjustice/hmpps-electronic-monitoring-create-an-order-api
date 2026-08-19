package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.stereotype.Component

@Component
class CorePersonRecordApiClient : PrisonerDetailsApi {
  override fun getPersionDetails(prisonNumber: String) {
    TODO("Not yet implemented")
  }
}
