package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import org.slf4j.LoggerFactory
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
    private val londonTimeZone: ZoneId = ZoneId.of("Europe/London")
    private val log = LoggerFactory.getLogger(Up3MonitoringOrder::class.java)
  }

  fun toCurfewConditions(versionId: UUID): CurfewConditions? {
    if (curfewStart.isBlank()) return null

    return CurfewConditions(
      versionId = versionId,
      startDate = parseDateTime(curfewStart),
      endDate = if (curfewEnd.isBlank()) null else parseDateTime(curfewEnd),
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

  fun toCurfewTimeTable(versionId: UUID): List<CurfewTimeTable> = curfewDuration.flatMap {
    it.toCurfewTimeTable(versionId)
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

  private fun parseDateTime(date: String): ZonedDateTime =
    LocalDateTime.parse(date, dateTimeFormatter).atZone(londonTimeZone)

  private fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(londonTimeZone)
}
