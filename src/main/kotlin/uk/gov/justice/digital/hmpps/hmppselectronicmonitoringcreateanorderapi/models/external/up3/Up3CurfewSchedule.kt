package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import java.time.DayOfWeek
import java.util.UUID

data class Up3CurfewSchedule(var day: String, var start: String, var end: String) {
  fun getTimeTable(versionId: UUID, address: String?): CurfewTimeTable? {
    val resolvedDay = dateOfWeek(day) ?: return null
    return CurfewTimeTable(
      versionId = versionId,
      dayOfWeek = resolvedDay,
      startTime = start,
      endTime = end,
      curfewAddress = address,
    )
  }

  private fun dateOfWeek(day: String): DayOfWeek? = when (day) {
    "Mo" -> DayOfWeek.MONDAY
    "Tu" -> DayOfWeek.TUESDAY
    "Wed" -> DayOfWeek.WEDNESDAY
    "Th" -> DayOfWeek.THURSDAY
    "Fr" -> DayOfWeek.FRIDAY
    "Sa" -> DayOfWeek.SATURDAY
    "Su" -> DayOfWeek.SUNDAY
    else -> null
  }
}
