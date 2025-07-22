package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import java.time.ZonedDateTime
import java.util.*

class CurfewConditionsBuilder(var versionId: UUID) {
  var startDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  var endDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  var curfewAddress: String = "PRIMARY,SECONDARY"

  fun build(): CurfewConditions = CurfewConditions(
    versionId = versionId,
    startDate = startDate,
    endDate = endDate,
    curfewAddress = curfewAddress,
  )
}
