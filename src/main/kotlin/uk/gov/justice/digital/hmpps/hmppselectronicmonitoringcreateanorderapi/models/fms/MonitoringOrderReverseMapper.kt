package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Dapo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Offence
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
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Offence as OffenceEntity

private val log = LoggerFactory.getLogger(MonitoringOrder::class.java)

fun MonitoringOrder.toCurfewConditions(versionId: UUID): CurfewConditions? {
  if (curfewStart.isNullOrBlank()) return null

  return CurfewConditions(
    versionId = versionId,
    startDate = FmsDates.parseDateTime(curfewStart ?: ""),
    endDate = FmsDates.parseDateTimeOrNull(curfewEnd ?: ""),
    curfewAdditionalDetails = curfewDescription,
  )
}

fun MonitoringOrder.toCurfewReleaseDateConditions(versionId: UUID): CurfewReleaseDateConditions? {
  if (conditionalReleaseDate.isNullOrBlank()) return null

  return CurfewReleaseDateConditions(
    versionId = versionId,
    releaseDate = FmsDates.parseDate(conditionalReleaseDate ?: ""),
    startTime = conditionalReleaseStartTime,
    endTime = conditionalReleaseEndTime,
  )
}

fun MonitoringOrder.toCurfewTimeTable(versionId: UUID): List<CurfewTimeTable> = curfewDuration.orEmpty().flatMap {
  it.toCurfewTimeTable(versionId)
}

fun MonitoringOrder.toTrailMonitoringConditions(versionId: UUID): TrailMonitoringConditions? {
  val trail = getTrailFromEnforceableCondition() ?: return null

  val deviceType = if (trail.condition?.contains("Non-Fitted") == true) DeviceType.NON_FITTED else DeviceType.FITTED

  return TrailMonitoringConditions(
    versionId = versionId,
    deviceType = deviceType,
    startDate = FmsDates.parseDateTime(trail.startDate ?: ""),
    endDate = FmsDates.parseDateTimeOrNull(trail.endDate ?: ""),
  )
}

fun MonitoringOrder.toAlcoholMonitoringConditions(versionId: UUID): AlcoholMonitoringConditions? {
  val alcohol = getAlcoholEnforceableCondition() ?: return null

  val alcoholType: AlcoholMonitoringType =
    if (abstinence == "Yes") AlcoholMonitoringType.ALCOHOL_ABSTINENCE else AlcoholMonitoringType.ALCOHOL_LEVEL

  return AlcoholMonitoringConditions(
    versionId = versionId,
    monitoringType = alcoholType,
    startDate = FmsDates.parseDateTime(alcohol.startDate ?: ""),
    endDate = FmsDates.parseDateTimeOrNull(alcohol.endDate ?: ""),
  )
}

fun MonitoringOrder.toEnforcementZoneConditions(versionId: UUID): List<EnforcementZoneConditions> {
  val zones =
    exclusionZones.map { it to EnforcementZoneType.EXCLUSION } +
      inclusionZones.map { it to EnforcementZoneType.INCLUSION }

  return zones.mapIndexed { zoneId, (zone, type) -> zone.toZoneConditions(versionId, type, zoneId) }
}

fun MonitoringOrder.toVariationDetails(versionId: UUID): VariationDetails? {
  if (orderVariationDate.isNullOrBlank()) return null

  return VariationDetails(
    versionId = versionId,
    variationDate = FmsDates.parseDateTime(orderVariationDate ?: ""),
    variationDetails = orderVariationDetails ?: "",
  )
}

fun MonitoringOrder.toOrderId(): UUID = UUID.fromString(orderId)

fun MonitoringOrder.toOrderVersionType(): RequestType? {
  val matched = RequestType.from(orderRequestType)
  if (matched == null) {
    log.error("Unmatched order request type: {}", orderRequestType)
  }
  return matched
}

fun MonitoringOrder.toCourtCaseReferenceNumber(): String? = magistrateCourtCaseReferenceNumber?.ifBlank { null }

fun MonitoringOrder.toProbationDeliveryUnit(versionId: UUID): ProbationDeliveryUnit? {
  if (pduResponsible.isNullOrBlank()) return null

  val matched = ProbationDeliveryUnitsDDv6.fromValue(pduResponsible)?.name
    ?: ProbationDeliveryUnits.fromValue(pduResponsible)?.name
  if (matched == null) {
    log.error("Unmatched probation delivery unit: {}", pduResponsible)
  }

  return ProbationDeliveryUnit(versionId = versionId, unit = matched)
}

fun MonitoringOrder.toOffences(versionId: UUID): List<OffenceEntity> = offences.orEmpty().mapNotNull { offenceData ->
  val matched = Offence.fromValue(offenceData.offence)
  if (matched == null) {
    log.error("Unmatched offence value: {}", offenceData.offence)
    return@mapNotNull null
  }
  OffenceEntity(
    versionId = versionId,
    offenceType = matched.name,
    offenceDate = FmsDates.parseDateOrNull(offenceData.offenceDate ?: ""),
  )
}

fun MonitoringOrder.toOffenceType(): String? {
  if (offence == "") return ""

  return acEligibleOffences?.firstOrNull()?.offence?.ifBlank { null }
}

fun MonitoringOrder.toMonitoringConditions(versionId: UUID): MonitoringConditions = MonitoringConditions(
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

fun MonitoringOrder.toInterestedParties(versionId: UUID): InterestedParties {
  val matchedNotifyingOrg = NotifyingOrganisationDDv5.fromValue(notifyingOrganization)
    ?: NotifyingOrganisation.fromValue(notifyingOrganization)
  if (!notifyingOrganization.isNullOrBlank() && matchedNotifyingOrg == null) {
    log.error("Unmatched notifying organisation: {}", notifyingOrganization)
  }

  val region = if (matchedNotifyingOrg == NotifyingOrganisationDDv5.HOME_OFFICE) "UKBA" else matchRegion(roRegion)

  return InterestedParties(
    versionId = versionId,
    responsibleOrganisation = matchOrLog("responsible organisation", responsibleOrganization) {
      ResponsibleOrganisation.fromValue(it)
    }?.name,
    responsibleOfficerName = responsibleOfficerName?.ifBlank { null },
    responsibleOfficerEmail = responsibleOfficerEmail?.ifBlank { null },
    responsibleOfficerPhoneNumber = responsibleOfficerPhone?.ifBlank { null },
    responsibleOrganisationEmail = roEmail?.ifBlank { null },
    responsibleOrganisationRegion = region,
    notifyingOrganisation = matchedNotifyingOrg?.name,
    notifyingOrganisationName = noName?.ifBlank { null },
    notifyingOrganisationEmail = noEmail?.ifBlank { null },
  )
}

fun MonitoringOrder.toDapo(versionId: UUID): List<Dapo> = dapoOrderClauseNumbers.orEmpty().map {
  Dapo(
    versionId = versionId,
    clause = it.dapoOrderClauseNumber?.ifBlank { null },
    date = FmsDates.parseDateOrNull(it.date ?: ""),
  )
}

fun MonitoringOrder.toInstallationAppointment(versionId: UUID): InstallationAppointment? {
  if (dateAndTimeInstallationWillTakePlace.isNullOrBlank() && tagAtSourceDetails.isNullOrBlank()) return null

  return InstallationAppointment(
    versionId = versionId,
    placeName = tagAtSourceDetails?.ifBlank { null },
    appointmentDate = FmsDates.parseDateTimeOrNull(dateAndTimeInstallationWillTakePlace ?: ""),
  )
}

fun MonitoringOrder.toInstallationAddress(versionId: UUID): Address? {
  if (installationAddress1.isNullOrBlank()) return null

  return Address(
    versionId = versionId,
    addressType = AddressType.INSTALLATION,
    addressLine1 = installationAddress1 ?: "",
    addressLine2 = installationAddress2 ?: "",
    addressLine3 = installationAddress3 ?: "",
    addressLine4 = installationAddress4 ?: "",
    postcode = installationAddressPostcode ?: "",
  )
}

private fun <T> matchOrLog(fieldDescription: String, value: String?, matcher: (String) -> T?): T? {
  if (value.isNullOrBlank()) return null

  val matched = matcher(value)
  if (matched == null) {
    log.error("Unmatched {} value: {}", fieldDescription, value)
  }
  return matched
}

private fun yesNo(value: String?): YesNoUnknown? = when (value) {
  "Yes" -> YesNoUnknown.YES
  "No" -> YesNoUnknown.NO
  else -> null
}

private fun trueFalseToYesNo(value: String?): YesNoUnknown? = when (value) {
  "true" -> YesNoUnknown.YES
  "false" -> YesNoUnknown.NO
  else -> null
}

private fun matchRegion(value: String?): String? {
  if (value.isNullOrBlank()) return null

  val matched = ProbationServiceRegion.fromValue(value)?.name
    ?: YouthJusticeServiceRegions.fromValue(value)?.name
    ?: PoliceAreasDDv6.fromValue(value)?.name
    ?: PoliceAreas.fromValue(value)?.name
  if (matched == null) {
    log.error("Unmatched responsible organisation region: {}", value)
  }
  return matched
}

private fun MonitoringOrder.getTrailFromEnforceableCondition(): EnforceableCondition? =
  enforceableCondition.orEmpty().find { it.isTrail() }

private fun MonitoringOrder.getAlcoholEnforceableCondition(): EnforceableCondition? =
  enforceableCondition.orEmpty().find { it.isAlcohol() }
