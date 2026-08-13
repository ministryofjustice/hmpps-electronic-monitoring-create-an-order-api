package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import java.util.UUID

data class Zone(
  val description: String? = "",
  val duration: String? = "",
  val start: String? = "",
  val end: String? = "",
) {
  fun toZoneConditions(versionId: UUID, type: EnforcementZoneType, id: Int): EnforcementZoneConditions =
    EnforcementZoneConditions(
      versionId = versionId,
      zoneType = type,
      description = description,
      duration = duration,
      startDate = FmsDates.parseDate(start ?: ""),
      endDate = FmsDates.parseDateOrNull(end ?: ""),
      zoneId = id,
    )
}
