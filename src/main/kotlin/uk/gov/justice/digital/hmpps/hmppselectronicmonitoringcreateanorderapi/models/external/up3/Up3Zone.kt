package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Up3Zone(var description: String, var duration: String, var start: String, var end: String) {

  companion object {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val londonTimeZone: ZoneId = ZoneId.of("Europe/London")
  }

  fun toZoneConditions(versionId: UUID, type: EnforcementZoneType, id: Int): EnforcementZoneConditions =
    EnforcementZoneConditions(
      versionId = versionId,
      zoneType = type,
      description = description,
      duration = duration,
      startDate = parseDate(start),
      endDate = parseDateOrNull(end),
      zoneId = id,
    )

  private fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(londonTimeZone)

  private fun parseDateOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDate(date) else null
}
