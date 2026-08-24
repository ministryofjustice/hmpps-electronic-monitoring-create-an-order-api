package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CorePersonRecord
import java.util.UUID

interface CorePersonRecordApi {
  fun getPersonByPrisonNumber(prisonNumber: String, versionId: UUID): CorePersonRecord
  fun getPersonByCrn(crn: String, versionId: UUID): CorePersonRecord
}
