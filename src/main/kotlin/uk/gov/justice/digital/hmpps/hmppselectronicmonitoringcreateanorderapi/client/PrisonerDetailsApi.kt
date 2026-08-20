package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.PrisonerDetails

interface PrisonerDetailsApi {
  fun getPrisonerDetails(prisonNumber: String): PrisonerDetails
}
