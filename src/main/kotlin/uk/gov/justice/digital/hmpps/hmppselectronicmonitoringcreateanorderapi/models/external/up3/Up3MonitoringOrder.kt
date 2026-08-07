package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Dapo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.PoliceAreas
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationDeliveryUnits
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthJusticeServiceRegions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.PoliceAreasDDv6
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.ProbationDeliveryUnitsDDv6
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.collections.mapNotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Offence as OffenceType

data class Up3MonitoringOrder(
  var orderId: String,
  var orderRequestType: String,
  var orderType: String,
  var magistrateCourtCaseReferenceNumber: String,
  var conditionType: String,
  var orderTypeDescription: String,
  var sentenceType: String,
  var pilot: String,
  var issp: String,
  var hdc: String,
  var releasedUnderPrarr: String,
  var dapolMissedInError: String,
  var offence: String,
  var offences: List<Up3Offence>,
  var acEligibleOffences: List<Up3Offence>,
  var responsibleOrganisation: String,
  var responsibleOfficerName: String,
  var responsibleOfficerEmail: String,
  var responsibleOfficerPhone: String,
  var roEmail: String,
  var roRegion: String,
  var notifyingOrganisation: String,
  var noName: String,
  var noEmail: String,
  var pduResponsible: String,
  var dapoOrderClauseNumbers: List<Up3Dapo>,
  var dateAndTimeInstallationWillTakePlace: String,
  var tagAtSourceDetails: String,
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
    private val log = LoggerFactory.getLogger(Up3MonitoringOrder::class.java)
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

  fun toOrderId(): UUID = UUID.fromString(orderId)

  fun toOrderVersionType(): RequestType? {
    val matched = RequestType.from(orderRequestType)
    if (matched == null) {
      log.error("Unmatched order request type: {}", orderRequestType)
    }
    return matched
  }

  fun toCourtCaseReferenceNumber(): String? = magistrateCourtCaseReferenceNumber.ifBlank { null }

  fun toProbationDeliveryUnit(versionId: UUID): ProbationDeliveryUnit? {
    if (pduResponsible.isBlank()) return null

    val matched = ProbationDeliveryUnitsDDv6.fromValue(pduResponsible)?.name
      ?: ProbationDeliveryUnits.fromValue(pduResponsible)?.name
    if (matched == null) {
      log.error("Unmatched probation delivery unit: {}", pduResponsible)
    }

    return ProbationDeliveryUnit(versionId = versionId, unit = matched)
  }

  fun toInstallationAndRisk(versionId: UUID): InstallationAndRisk? {
    if (offence.isBlank()) return null

    val matched = OffenceType.fromValue(offence)
    if (matched == null) {
      log.error("Unmatched offence value: {}", offence)
    }

    return InstallationAndRisk(versionId = versionId, offence = matched?.name)
  }

  fun toOffences(versionId: UUID): List<Offence> = offences.mapNotNull { up3Offence ->
    val matched = OffenceType.fromValue(up3Offence.offence)
    if (matched == null) {
      log.error("Unmatched offence value: {}", up3Offence.offence)
      return@mapNotNull null
    }
    Offence(versionId = versionId, offenceType = matched.name, offenceDate = parseDateOrNull(up3Offence.offenceDate))
  }

  fun toOffenceType(): String? = acEligibleOffences.firstOrNull()?.offence?.ifBlank { null }

  fun toMonitoringConditions(versionId: UUID): MonitoringConditions = MonitoringConditions(
    versionId = versionId,
    conditionType = matchOrLog("condition type", conditionType) { MonitoringConditionType.from(it) },
    orderType = matchOrLog("order type", orderType) { OrderType.from(it) },
    orderTypeDescription = matchOrLog("order type description", orderTypeDescription) { OrderTypeDescription.from(it) },
    pilot = matchOrLog("pilot", pilot) { Pilot.from(it) },
    issp = yesNo(issp),
    hdc = yesNo(hdc),
    prarr = trueFalseToYesNo(releasedUnderPrarr),
    dapolMissedInError = if (dapolMissedInError == "true") YesNoUnknown.YES else null,
    offenceType = toOffenceType(),
  )

  fun toInterestedParties(versionId: UUID): InterestedParties {
    val matchedNotifyingOrg = NotifyingOrganisationDDv5.fromValue(notifyingOrganisation)
      ?: NotifyingOrganisation.fromValue(notifyingOrganisation)
    if (notifyingOrganisation.isNotBlank() && matchedNotifyingOrg == null) {
      log.error("Unmatched notifying organisation: {}", notifyingOrganisation)
    }

    val region = if (matchedNotifyingOrg == NotifyingOrganisationDDv5.HOME_OFFICE) "UKBA" else matchRegion(roRegion)

    return InterestedParties(
      versionId = versionId,
      responsibleOrganisation = matchOrLog("responsible organisation", responsibleOrganisation) {
        ResponsibleOrganisation.fromValue(it)
      }?.name,
      responsibleOfficerName = responsibleOfficerName.ifBlank { null },
      responsibleOfficerEmail = responsibleOfficerEmail.ifBlank { null },
      responsibleOfficerPhoneNumber = responsibleOfficerPhone.ifBlank { null },
      responsibleOrganisationEmail = roEmail.ifBlank { null },
      responsibleOrganisationRegion = region,
      notifyingOrganisation = matchedNotifyingOrg?.name,
      notifyingOrganisationName = noName.ifBlank { null },
      notifyingOrganisationEmail = noEmail.ifBlank { null },
    )
  }

  fun toDapo(versionId: UUID): List<Dapo> = dapoOrderClauseNumbers.map {
    Dapo(versionId = versionId, clause = it.dapoOrderClauseNumber.ifBlank { null }, date = parseDateOrNull(it.date))
  }

  fun toInstallationAppointment(versionId: UUID): InstallationAppointment? {
    if (dateAndTimeInstallationWillTakePlace.isBlank() && tagAtSourceDetails.isBlank()) return null

    return InstallationAppointment(
      versionId = versionId,
      placeName = tagAtSourceDetails.ifBlank { null },
      appointmentDate = parseDateTimeOrNull(dateAndTimeInstallationWillTakePlace),
    )
  }

  private fun <T> matchOrLog(fieldDescription: String, value: String, matcher: (String) -> T?): T? {
    if (value.isBlank()) return null

    val matched = matcher(value)
    if (matched == null) {
      log.error("Unmatched {} value: {}", fieldDescription, value)
    }
    return matched
  }

  private fun yesNo(value: String): YesNoUnknown? = when (value) {
    "Yes" -> YesNoUnknown.YES
    "No" -> YesNoUnknown.NO
    else -> null
  }

  private fun trueFalseToYesNo(value: String): YesNoUnknown? = when (value) {
    "true" -> YesNoUnknown.YES
    "false" -> YesNoUnknown.NO
    else -> null
  }

  private fun matchRegion(value: String): String? {
    if (value.isBlank()) return null

    val matched = ProbationServiceRegion.fromValue(value)?.name
      ?: YouthJusticeServiceRegions.fromValue(value)?.name
      ?: PoliceAreasDDv6.fromValue(value)?.name
      ?: PoliceAreas.fromValue(value)?.name
    if (matched == null) {
      log.error("Unmatched responsible organisation region: {}", value)
    }
    return matched
  }

  private fun parseDateTime(date: String): ZonedDateTime =
    LocalDateTime.parse(date, dateTimeFormatter).atZone(londonTimeZone)

  private fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(londonTimeZone)

  private fun parseDateTimeOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDateTime(date) else null

  private fun parseDateOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDate(date) else null

  private fun getTrailFromEnforceableCondition(): Up3EnforceableCondition? = enforceableCondition.find {
    it.isTrail()
  }

  private fun getAlcoholEnforceableCondition(): Up3EnforceableCondition? = enforceableCondition.find {
    it.isAlcohol()
  }
}
