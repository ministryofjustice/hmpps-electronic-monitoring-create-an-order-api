package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import java.time.ZonedDateTime
import java.util.*

class MonitoringConditionsTrailBuilder(var versionId: UUID) {
  var startDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  var endDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)

  fun build(): TrailMonitoringConditions = TrailMonitoringConditions(
    versionId = versionId,
    startDate = startDate,
    endDate = endDate,
  )
}
