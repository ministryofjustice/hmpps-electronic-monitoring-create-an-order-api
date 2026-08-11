package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import java.util.UUID

data class CurfewSchedule(
  val location: String? = "",
  val allday: String? = "",
  val schedule: MutableList<Schedule>? = mutableListOf(),
) {
  companion object {
    private val log = LoggerFactory.getLogger(CurfewSchedule::class.java)
  }

  fun toCurfewTimeTable(versionId: UUID): List<CurfewTimeTable> {
    val address = addressType(location ?: "") ?: return emptyList()

    return schedule.orEmpty().mapNotNull { it.getTimeTable(versionId, address) }
  }

  private fun addressType(location: String): String? = when (location) {
    "primary" -> "PRIMARY_ADDRESS"
    "secondary" -> "SECONDARY_ADDRESS"
    "tertiary" -> "TERTIARY_ADDRESS"
    else -> {
      log.error("Invalid location string: {}", location)
      null
    }
  }
}
