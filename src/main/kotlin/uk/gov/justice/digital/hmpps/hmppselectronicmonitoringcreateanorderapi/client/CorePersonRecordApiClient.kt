package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.PrisonerRecord

@Component
class CorePersonRecordApiClient : PrisonerDetailsApi {
  override fun getPrisonerDetails(prisonNumber: String): PrisonerRecord {
    TODO("Not yet implemented")
  }
}
