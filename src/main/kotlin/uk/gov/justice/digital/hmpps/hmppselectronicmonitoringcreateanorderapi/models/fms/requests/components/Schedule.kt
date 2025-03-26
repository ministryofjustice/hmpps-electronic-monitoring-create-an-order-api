package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import java.time.DayOfWeek

data class Schedule(val day: String? = "", val start: String? = "", val end: String? = "") {
  companion object {
    private fun getShortDayString(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
      DayOfWeek.MONDAY -> "Mo"
      DayOfWeek.TUESDAY -> "Tu"
      DayOfWeek.WEDNESDAY -> "Wed"
      DayOfWeek.THURSDAY -> "Th"
      DayOfWeek.FRIDAY -> "Fr"
      DayOfWeek.SATURDAY -> "Sa"
      DayOfWeek.SUNDAY -> "Su"
    }
    fun fromCurfewTimeTable(curfewTimeTable: CurfewTimeTable): Schedule =
      Schedule(getShortDayString(curfewTimeTable.dayOfWeek), curfewTimeTable.startTime, curfewTimeTable.endTime)
  }
}
