package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
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
  var exclusionZones: List<Up3Zone>,
  var inclusionZones: List<Up3Zone>,
) {
  companion object {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val londonTimeZone: ZoneId = ZoneId.of("Europe/London")
  }

  fun toCurfewConditions(versionId: UUID): CurfewConditions? {
    if (curfewStart.isBlank()) return null

    return CurfewConditions(
      versionId = versionId,
      startDate = parseDateTime(curfewStart),
      endDate = parseDateTimeOrNull(curfewEnd),
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
    val trail = getTrailFromEnforceableCondition() ?: return null

    val deviceType = if (trail.condition.contains("Non-Fitted")) DeviceType.NON_FITTED else DeviceType.FITTED

    return TrailMonitoringConditions(
      versionId = versionId,
      deviceType = deviceType,
      startDate = parseDateTime(trail.startDate),
      endDate = parseDateTimeOrNull(trail.endDate),
    )
  }

  fun toAlcoholMonitoringConditions(versionId: UUID): AlcoholMonitoringConditions? {
    val alcohol = getAlcoholEnforceableCondition() ?: return null

    val alcoholType: AlcoholMonitoringType =
      if (abstinence == "Yes") AlcoholMonitoringType.ALCOHOL_ABSTINENCE else AlcoholMonitoringType.ALCOHOL_LEVEL

    return AlcoholMonitoringConditions(
      versionId = versionId,
      monitoringType = alcoholType,
      startDate = parseDateTime(alcohol.startDate),
      endDate = parseDateTimeOrNull(alcohol.endDate),
    )
  }

  fun toEnforcementZoneConditions(versionId: UUID): List<EnforcementZoneConditions> {
    val zones =
      exclusionZones.map { it to EnforcementZoneType.EXCLUSION } +
        inclusionZones.map { it to EnforcementZoneType.INCLUSION }

    return zones.mapIndexed { zoneId, (zone, type) -> zone.toZoneConditions(versionId, type, zoneId) }
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

  private fun parseDateTimeOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDateTime(date) else null

  private fun getTrailFromEnforceableCondition(): Up3EnforceableCondition? = enforceableCondition.find {
    it.isTrail()
  }

  private fun getAlcoholEnforceableCondition(): Up3EnforceableCondition? = enforceableCondition.find {
    it.isAlcohol()
  }
}
