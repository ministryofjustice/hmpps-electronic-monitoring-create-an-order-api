package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Up3MonitoringOrder(
  var orderId: String,
  var orderRequestType: String,
  var orderType: String,
  var curfewStart: String,
  var curfewEnd: String,
  var curfewDescription: String,
  var conditionalReleaseDate: String,
  var conditionalReleaseStartTime: String,
  var conditionalReleaseEndTime: String,
  var curfewDuration: List<Up3CurfewDuration>,
  var enforceableCondition: List<Up3EnforceableCondition>,
  var abstinence: String,
  var orderVariationDate: String,
  var orderVariationDetails: String,
) {
  companion object {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  }

  fun toCurfewConditions(versionId: UUID): CurfewConditions? {
    if (curfewStart.isBlank()) return null

    return CurfewConditions(
      versionId = versionId,
      startDate = parseDateTime(curfewStart),
      endDate = parseDateTime(curfewEnd),
      curfewAdditionalDetails = curfewDescription,
    )
  }

  fun toCurfewReleaseDateConditions(versionId: UUID): CurfewReleaseDateConditions? {
    if (conditionalReleaseDate.isBlank()) return null

    return CurfewReleaseDateConditions(
      versionId = versionId,
      releaseDate = parseDate(conditionalReleaseDate),
      startTime = conditionalReleaseStartTime,
      endTime = conditionalReleaseEndTime,
    )
  }

  fun toCurfewTimeTable(versionId: UUID): MutableList<CurfewTimeTable> {
    val result = mutableListOf<CurfewTimeTable>()

    curfewDuration.forEach { curfew ->
      curfew.schedule.forEach {
        val timeTable = it.getTimeTable(versionId, addressType(curfew.location))

        if (timeTable != null) result.add(timeTable)
      }
    }

    return result
  }

  fun toTrailMonitoringConditions(versionId: UUID): TrailMonitoringConditions {
    TODO("Not yet implemented")
  }

  fun toAlcoholMonitoringConditions(versionId: UUID): AlcoholMonitoringConditions {
    TODO("Not yet implemented")
  }

  fun toVariationDetails(versionId: UUID): VariationDetails {
    TODO("Not yet implemented")
  }

  private fun addressType(location: String): String? = when (location) {
    "primary" -> "PRIMARY_ADDRESS"
    "secondary" -> "SECONDARY_ADDRESS"
    "tertiary" -> "TERTIARY_ADDRESS"
    else -> null
  }

  private fun parseDateTime(date: String): ZonedDateTime =
    LocalDateTime.parse(date, dateTimeFormatter).atZone(ZoneId.of("Europe/London"))

  private fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(ZoneId.of("Europe/London"))
}
