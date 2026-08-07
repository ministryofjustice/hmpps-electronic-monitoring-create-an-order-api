package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceType
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

  fun toTrailMonitoringConditions(versionId: UUID): TrailMonitoringConditions? {
    val trail = getTrailFromEnforcableCondition() ?: return null

    val deviceType = if (trail.condition.contains("Non-Fitted")) DeviceType.NON_FITTED else DeviceType.FITTED

    return TrailMonitoringConditions(
      versionId = versionId,
      deviceType = deviceType,
      startDate = parseDateTime(trail.startDate),
      endDate = if (trail.endDate.isNotBlank()) parseDateTime(trail.endDate) else null,
    )
  }

  fun toAlcoholMonitoringConditions(versionId: UUID): AlcoholMonitoringConditions? {
    val alcohol = getAlcoholEnforcableCondition() ?: return null

    val alcoholType: AlcoholMonitoringType =
      if (alcohol.condition == "AAMR") AlcoholMonitoringType.ALCOHOL_LEVEL else AlcoholMonitoringType.ALCOHOL_ABSTINENCE

    return AlcoholMonitoringConditions(
      versionId = versionId,
      monitoringType = alcoholType,
      startDate = parseDateTime(alcohol.startDate),
      endDate = if (alcohol.endDate.isNotBlank()) parseDateTime(alcohol.endDate) else null,
    )
  }

  fun toVariationDetails(versionId: UUID): VariationDetails? {
    if (orderVariationDate.isBlank()) return null

    return VariationDetails(
      versionId = versionId,
      variationDate = parseDateTime(orderVariationDate),
      variationDetails = orderVariationDetails,
    )
  }

  private fun parseDateTime(date: String): ZonedDateTime =
    LocalDateTime.parse(date, dateTimeFormatter).atZone(londonTimeZone)

  private fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(londonTimeZone)

  private fun getTrailFromEnforcableCondition(): Up3EnforceableCondition? = enforceableCondition.find {
    it.condition.contains("Location Monitoring")
  }

  private fun getAlcoholEnforcableCondition(): Up3EnforceableCondition? = enforceableCondition.find {
    it.condition == "AAMR" || it.condition == "AML"
  }
}
