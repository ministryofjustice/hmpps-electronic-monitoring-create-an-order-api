package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.PrisonerRecord
import java.util.UUID

interface PrisonerDetailsApi {
  fun getPrisonerDetails(prisonNumber: String, versionId: UUID): PrisonerRecord
}
