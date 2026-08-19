package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.PrisonerDetails

interface PrisonerDetailsApi {
  fun getPrisonerDetails(prisonNumber: String): PrisonerDetails
}
