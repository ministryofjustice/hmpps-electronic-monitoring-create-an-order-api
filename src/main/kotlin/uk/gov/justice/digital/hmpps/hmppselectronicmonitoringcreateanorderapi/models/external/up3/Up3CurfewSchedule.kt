package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import java.time.DayOfWeek
import java.util.UUID

data class Up3CurfewSchedule(var day: String, var start: String, var end: String) {
  companion object {
    private val log = LoggerFactory.getLogger(Up3CurfewSchedule::class.java)
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
