package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import java.time.ZonedDateTime
import java.util.*

class EnforcementZoneConditionsBuilder(var versionId: UUID) {
  var description: String = "Mock Exclusion Zone"
  var duration: String = "Mock Exclusion Duration"
  var startDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  var endDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  var zoneType: EnforcementZoneType = EnforcementZoneType.EXCLUSION
  var fileId = UUID.randomUUID()
  var fileName = "MockMapFile.jpeg"

  fun build(): EnforcementZoneConditions {
    return EnforcementZoneConditions(
      versionId = versionId,
      description = description,
      duration = duration,
      startDate = startDate,
      endDate = endDate,
      zoneType = zoneType,
      fileId = fileId,
      fileName = fileName,
    )
  }
}
