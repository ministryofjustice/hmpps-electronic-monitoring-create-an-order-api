package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.PrisonerDetails

@Component
class CorePersonRecordApiClient : PrisonerDetailsApi {
  override fun getPrisonerDetails(prisonNumber: String): PrisonerDetails {
    TODO("Not yet implemented")
  }
}
