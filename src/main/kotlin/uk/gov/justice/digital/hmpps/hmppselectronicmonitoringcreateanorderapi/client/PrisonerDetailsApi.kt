package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.PrisonerRecord

interface PrisonerDetailsApi {
  fun getPrisonerDetails(prisonNumber: String): PrisonerRecord
}
