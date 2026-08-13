package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import java.time.DayOfWeek
import java.util.UUID

data class Schedule(val day: String? = "", val start: String? = "", val end: String? = "") {
  companion object {
    private val log = LoggerFactory.getLogger(Schedule::class.java)

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

  fun getTimeTable(versionId: UUID, address: String?): CurfewTimeTable? {
    val resolvedDay = dateOfWeek() ?: return null

    return CurfewTimeTable(
      versionId = versionId,
      dayOfWeek = resolvedDay,
      startTime = start,
      endTime = end,
      curfewAddress = address,
    )
  }

  private fun dateOfWeek(): DayOfWeek? = when (day) {
    "Mo" -> DayOfWeek.MONDAY
    "Tu" -> DayOfWeek.TUESDAY
    "Wed" -> DayOfWeek.WEDNESDAY
    "Th" -> DayOfWeek.THURSDAY
    "Fr" -> DayOfWeek.FRIDAY
    "Sa" -> DayOfWeek.SATURDAY
    "Su" -> DayOfWeek.SUNDAY
    else -> {
      log.error("Invalid day of the week: {}", day)
      null
    }
  }
}
