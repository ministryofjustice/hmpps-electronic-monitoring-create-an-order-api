package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import java.util.UUID

data class Up3CurfewDuration(var location: String, var allday: String, var schedule: List<Up3CurfewSchedule>) {
  companion object {
    private val log = LoggerFactory.getLogger(Up3CurfewDuration::class.java)
  }

  fun toCurfewTimeTable(versionId: UUID): List<CurfewTimeTable> {
    val address = addressType(location) ?: return emptyList()
    return schedule.mapNotNull { it.getTimeTable(versionId, address) }
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
