package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationDeliveryUnits
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthJusticeServiceRegions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.ProbationDeliveryUnitsDDv6
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.CurfewSchedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DapoClause
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.OffenceData
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Schedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Zone
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class MonitoringOrderReverseMappingTest {

  val monitoringOrderDto =
    MonitoringOrder(
      orderId = "order-1",
      orderRequestType = "New Order",
      orderType = "Standalone",
      curfewStart = "",
      curfewEnd = "",
      curfewDescription = "",
      conditionalReleaseDate = "",
      conditionalReleaseStartTime = "",
      conditionalReleaseEndTime = "",
      abstinence = "",
      orderVariationDate = "",
      orderVariationDetails = "",
      magistrateCourtCaseReferenceNumber = "",
      conditionType = "",
      orderTypeDescription = "",
      sentenceType = "",
      pilot = "",
      issp = "",
      hdc = "",
      releasedUnderPrarr = "",
      dapolMissedInError = "",
      offence = "",
      responsibleOrganization = "",
      responsibleOfficerName = "",
      responsibleOfficerEmail = "",
      responsibleOfficerPhone = "",
      roEmail = "",
      roRegion = "",
      notifyingOrganization = "",
      noName = "",
      noEmail = "",
      pduResponsible = "",
      dateAndTimeInstallationWillTakePlace = "",
      tagAtSourceDetails = "",
    )
  val versionId: UUID = UUID.randomUUID()

  @Test
  fun `it should map curfew conditions when curfew start is present`() {
    val dto = monitoringOrderDto.copy(
      curfewStart = "2000-07-01 08:00:00",
      curfewEnd = "2001-07-01 08:00:00",
      curfewDescription = "wears a tag",
    )
    val curfew = dto.toCurfewConditions(versionId)

    assertThat(curfew).isNotNull
    assertThat(curfew!!.versionId).isEqualTo(versionId)
    assertThat(curfew.startDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, ZoneId.of("Europe/London")),
    )
    assertThat(curfew.endDate).isEqualTo(
      ZonedDateTime.of(2001, 7, 1, 8, 0, 0, 0, ZoneId.of("Europe/London")),
    )
    assertThat(curfew.curfewAdditionalDetails).isEqualTo("wears a tag")
  }

  @Test
  fun `it should not build curfew conditions when curfew start is blank`() {
    assertThat(monitoringOrderDto.toCurfewConditions(versionId)).isNull()
  }

  @Test
  fun `it should map curfew release date conditions when release date is present`() {
    val dto = monitoringOrderDto.copy(
      conditionalReleaseDate = "2000-07-01",
      conditionalReleaseStartTime = "08:00",
      conditionalReleaseEndTime = "09:00",
    )
    val releaseConditions = dto.toCurfewReleaseDateConditions(versionId)

    assertThat(releaseConditions).isNotNull
    assertThat(releaseConditions!!.versionId).isEqualTo(versionId)
    assertThat(releaseConditions.releaseDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 0, 0, 0, 0, ZoneId.of("Europe/London")),
    )
    assertThat(releaseConditions.startTime).isEqualTo("08:00")
    assertThat(releaseConditions.endTime).isEqualTo("09:00")
  }

  @Test
  fun `it should not build curfew release date conditions when release date is blank`() {
    assertThat(monitoringOrderDto.toCurfewReleaseDateConditions(versionId)).isNull()
  }

  @Test
  fun `it should map curfew timetable rows for each schedule entry`() {
    val dto = monitoringOrderDto.copy(
      curfewDuration = mutableListOf(
        CurfewSchedule(
          location = "primary",
          allday = "",
          schedule = mutableListOf(
            Schedule(day = "Mo", start = "08:00", end = "09:00"),
            Schedule(day = "Wed", start = "10:00", end = "11:00"),
          ),
        ),
      ),
    )
    val rows = dto.toCurfewTimeTable(versionId)

    assertThat(rows).hasSize(2)
    assertThat(rows[0].versionId).isEqualTo(versionId)
    assertThat(rows[0].dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
    assertThat(rows[0].startTime).isEqualTo("08:00")
    assertThat(rows[0].endTime).isEqualTo("09:00")
    assertThat(rows[0].curfewAddress).isEqualTo("PRIMARY_ADDRESS")
    assertThat(rows[1].dayOfWeek).isEqualTo(DayOfWeek.WEDNESDAY)
  }

  @Test
  fun `it should map secondary and tertiary curfew timetable locations`() {
    val dto = monitoringOrderDto.copy(
      curfewDuration = mutableListOf(
        CurfewSchedule("secondary", "", mutableListOf(Schedule("Tu", "08:00", "09:00"))),
        CurfewSchedule("tertiary", "", mutableListOf(Schedule("Th", "08:00", "09:00"))),
      ),
    )
    val rows = dto.toCurfewTimeTable(versionId)

    assertThat(rows).hasSize(2)
    assertThat(rows[0].curfewAddress).isEqualTo("SECONDARY_ADDRESS")
    assertThat(rows[0].dayOfWeek).isEqualTo(DayOfWeek.TUESDAY)
    assertThat(rows[1].curfewAddress).isEqualTo("TERTIARY_ADDRESS")
    assertThat(rows[1].dayOfWeek).isEqualTo(DayOfWeek.THURSDAY)
  }

  @Test
  fun `it should drop unmatched day codes and not throw`() {
    val dto = monitoringOrderDto.copy(
      curfewDuration = mutableListOf(
        CurfewSchedule(
          "primary",
          "",
          mutableListOf(
            Schedule("Mo", "08:00", "09:00"),
            Schedule("not a real day", "08:00", "09:00"),
          ),
        ),
      ),
    )
    val rows = dto.toCurfewTimeTable(versionId)

    assertThat(rows).hasSize(1)
    assertThat(rows[0].dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
  }

  @Test
  fun `it should return no curfew timetable rows when curfew duration is empty`() {
    assertThat(monitoringOrderDto.toCurfewTimeTable(versionId)).isEmpty()
  }

  @Test
  fun `it should map trail monitoring conditions for a fitted device`() {
    val dto = monitoringOrderDto.copy(
      enforceableCondition = mutableListOf(
        EnforceableCondition(
          "Location Monitoring (Fitted Device)",
          "2000-07-01 08:00:00",
          "2001-07-01 08:00:00",
        ),
      ),
    )
    val trail = dto.toTrailMonitoringConditions(versionId)

    assertThat(trail).isNotNull
    assertThat(trail!!.versionId).isEqualTo(versionId)
    assertThat(trail.deviceType).isEqualTo(DeviceType.FITTED)
    assertThat(trail.startDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, ZoneId.of("Europe/London")),
    )
  }

  @Test
  fun `it should map trail monitoring conditions for a non-fitted device`() {
    val dto = monitoringOrderDto.copy(
      enforceableCondition = mutableListOf(
        EnforceableCondition(
          "Location Monitoring (using Non-Fitted Device)",
          "2000-07-01 08:00:00",
          "2001-07-01 08:00:00",
        ),
      ),
    )
    val trail = dto.toTrailMonitoringConditions(versionId)

    assertThat(trail!!.deviceType).isEqualTo(DeviceType.NON_FITTED)
  }

  @Test
  fun `it should not build trail monitoring conditions when no matching condition is present`() {
    assertThat(monitoringOrderDto.toTrailMonitoringConditions(versionId)).isNull()
  }

  @Test
  fun `it should map alcohol monitoring conditions when an AML condition is present`() {
    val dto = monitoringOrderDto.copy(
      abstinence = "Yes",
      enforceableCondition = mutableListOf(
        EnforceableCondition("AML", "2000-07-01 08:00:00", "2001-07-01 08:00:00"),
      ),
    )
    val alcohol = dto.toAlcoholMonitoringConditions(versionId)

    assertThat(alcohol).isNotNull
    assertThat(alcohol!!.versionId).isEqualTo(versionId)
    assertThat(alcohol.monitoringType).isEqualTo(AlcoholMonitoringType.ALCOHOL_ABSTINENCE)
    assertThat(alcohol.startDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, ZoneId.of("Europe/London")),
    )
  }

  @Test
  fun `it should map alcohol monitoring conditions when an AAMR condition is present`() {
    val dto = monitoringOrderDto.copy(
      abstinence = "No",
      enforceableCondition = mutableListOf(
        EnforceableCondition("AAMR", "2000-07-01 08:00:00", "2001-07-01 08:00:00"),
      ),
    )
    val alcohol = dto.toAlcoholMonitoringConditions(versionId)

    assertThat(alcohol).isNotNull
    assertThat(alcohol!!.monitoringType).isEqualTo(AlcoholMonitoringType.ALCOHOL_LEVEL)
  }

  @Test
  fun `it should not build alcohol monitoring conditions when no matching condition is present`() {
    assertThat(monitoringOrderDto.toAlcoholMonitoringConditions(versionId)).isNull()
  }

  @Test
  fun `it should map exclusion zones with zoneType EXCLUSION`() {
    val dto = monitoringOrderDto.copy(
      exclusionZones = mutableListOf(
        Zone(description = "no go area", duration = "2 weeks", start = "2000-07-01", end = "2001-07-01"),
      ),
    )
    val rows = dto.toEnforcementZoneConditions(versionId)

    assertThat(rows).hasSize(1)
    assertThat(rows[0].versionId).isEqualTo(versionId)
    assertThat(rows[0].zoneType).isEqualTo(EnforcementZoneType.EXCLUSION)
    assertThat(rows[0].zoneId).isEqualTo(0)
    assertThat(rows[0].description).isEqualTo("no go area")
    assertThat(rows[0].duration).isEqualTo("2 weeks")
    assertThat(rows[0].startDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 0, 0, 0, 0, ZoneId.of("Europe/London")),
    )
    assertThat(rows[0].endDate).isEqualTo(
      ZonedDateTime.of(2001, 7, 1, 0, 0, 0, 0, ZoneId.of("Europe/London")),
    )
  }

  @Test
  fun `it should map inclusion zones with zoneType INCLUSION`() {
    val dto = monitoringOrderDto.copy(
      inclusionZones = mutableListOf(
        Zone(description = "must stay here", duration = "1 week", start = "2000-07-01", end = "2001-07-01"),
      ),
    )
    val rows = dto.toEnforcementZoneConditions(versionId)

    assertThat(rows).hasSize(1)
    assertThat(rows[0].zoneType).isEqualTo(EnforcementZoneType.INCLUSION)
    assertThat(rows[0].description).isEqualTo("must stay here")
    assertThat(rows[0].duration).isEqualTo("1 week")
  }

  @Test
  fun `it should map exclusion and inclusion zones together, exclusion rows first`() {
    val dto = monitoringOrderDto.copy(
      exclusionZones = mutableListOf(Zone("exclusion 1", "", "2000-07-01", "")),
      inclusionZones = mutableListOf(Zone("inclusion 1", "", "2000-07-01", "")),
    )
    val rows = dto.toEnforcementZoneConditions(versionId)

    assertThat(rows).hasSize(2)
    assertThat(rows[0].zoneType).isEqualTo(EnforcementZoneType.EXCLUSION)
    assertThat(rows[1].zoneType).isEqualTo(EnforcementZoneType.INCLUSION)
  }

  @Test
  fun `it should assign a contiguous 0-based zoneId across exclusion and inclusion zones`() {
    val dto = monitoringOrderDto.copy(
      exclusionZones = mutableListOf(
        Zone("exclusion 1", "", "2000-07-01", ""),
        Zone("exclusion 2", "", "2000-07-01", ""),
      ),
      inclusionZones = mutableListOf(Zone("inclusion 1", "", "2000-07-01", "")),
    )
    val rows = dto.toEnforcementZoneConditions(versionId)

    assertThat(rows).hasSize(3)
    assertThat(rows[0].zoneId).isEqualTo(0)
    assertThat(rows[1].zoneId).isEqualTo(1)
    assertThat(rows[2].zoneId).isEqualTo(2)
  }

  @Test
  fun `it should leave end date null when zone end is blank`() {
    val dto = monitoringOrderDto.copy(
      exclusionZones = mutableListOf(Zone("no go area", "", "2000-07-01", "")),
    )
    val rows = dto.toEnforcementZoneConditions(versionId)

    assertThat(rows[0].endDate).isNull()
  }

  @Test
  fun `it should return no enforcement zone rows when no zones are present`() {
    assertThat(monitoringOrderDto.toEnforcementZoneConditions(versionId)).isEmpty()
  }

  @Test
  fun `it should map variation details when order variation date is present`() {
    val dto = monitoringOrderDto.copy(
      orderVariationDate = "2000-07-01 08:00:00",
      orderVariationDetails = "changed curfew hours",
    )
    val variation = dto.toVariationDetails(versionId)

    assertThat(variation).isNotNull
    assertThat(variation!!.versionId).isEqualTo(versionId)
    assertThat(variation.variationDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, ZoneId.of("Europe/London")),
    )
    assertThat(variation.variationDetails).isEqualTo("changed curfew hours")
  }

  @Test
  fun `it should not build variation details when order variation date is blank`() {
    assertThat(monitoringOrderDto.toVariationDetails(versionId)).isNull()
  }

  @Test
  fun `it should map order id`() {
    val id = UUID.randomUUID()
    val dto = monitoringOrderDto.copy(orderId = id.toString())

    assertThat(dto.toOrderId()).isEqualTo(id)
  }

  @Test
  fun `it should map order request type`() {
    val dto = monitoringOrderDto.copy(orderRequestType = "New Order")

    assertThat(dto.toOrderVersionType()).isEqualTo(RequestType.REQUEST)
  }

  @Test
  fun `it should leave order version type null and not throw when unmatched`() {
    val dto = monitoringOrderDto.copy(orderRequestType = "not a real request type")

    assertThat(dto.toOrderVersionType()).isNull()
  }

  @Test
  fun `it should map court case reference number when present`() {
    val dto = monitoringOrderDto.copy(magistrateCourtCaseReferenceNumber = "CCRN123")

    assertThat(dto.toCourtCaseReferenceNumber()).isEqualTo("CCRN123")
  }

  @Test
  fun `it should leave court case reference number null when blank`() {
    assertThat(monitoringOrderDto.toCourtCaseReferenceNumber()).isNull()
  }

  @Test
  fun `it should map probation delivery unit preferring the DDv6 value`() {
    val dto = monitoringOrderDto.copy(pduResponsible = "Salford and Trafford")
    val pdu = dto.toProbationDeliveryUnit(versionId)

    assertThat(pdu).isNotNull
    assertThat(pdu!!.versionId).isEqualTo(versionId)
    assertThat(pdu.unit).isEqualTo(ProbationDeliveryUnitsDDv6.SALFORD_AND_TRAFFORD.name)
  }

  @Test
  fun `it should fall back to the legacy probation delivery unit when the DDv6 value does not match`() {
    val dto = monitoringOrderDto.copy(pduResponsible = "Salford")
    val pdu = dto.toProbationDeliveryUnit(versionId)

    assertThat(pdu!!.unit).isEqualTo(ProbationDeliveryUnits.SALFORD.name)
  }

  @Test
  fun `it should not build probation delivery unit when pdu responsible is blank`() {
    assertThat(monitoringOrderDto.toProbationDeliveryUnit(versionId)).isNull()
  }

  @Test
  fun `it should build probation delivery unit with a null unit when unmatched`() {
    val dto = monitoringOrderDto.copy(pduResponsible = "not a real pdu")
    val pdu = dto.toProbationDeliveryUnit(versionId)

    assertThat(pdu).isNotNull
    assertThat(pdu!!.unit).isNull()
  }

  @Test
  fun `it should map offence onto installation and risk`() {
    val dto = monitoringOrderDto.copy(offence = "Robbery")
    val installationAndRisk = dto.toInstallationAndRisk(versionId)

    assertThat(installationAndRisk).isNotNull
    assertThat(installationAndRisk!!.versionId).isEqualTo(versionId)
    assertThat(installationAndRisk.offence).isEqualTo(Offence.ROBBERY.name)
  }

  @Test
  fun `it should not build installation and risk when offence is blank`() {
    assertThat(monitoringOrderDto.toInstallationAndRisk(versionId)).isNull()
  }

  @Test
  fun `it should map offences to offence rows`() {
    val dto = monitoringOrderDto.copy(
      offences = mutableListOf(OffenceData(offence = "Robbery", offenceDate = "2000-07-01")),
    )
    val offences = dto.toOffences(versionId)

    assertThat(offences).hasSize(1)
    assertThat(offences[0].versionId).isEqualTo(versionId)
    assertThat(offences[0].offenceType).isEqualTo(Offence.ROBBERY.name)
    assertThat(offences[0].offenceDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 0, 0, 0, 0, ZoneId.of("Europe/London")),
    )
  }

  @Test
  fun `it should drop unmatched offences and not throw`() {
    val dto = monitoringOrderDto.copy(
      offences = mutableListOf(OffenceData(offence = "not a real offence", offenceDate = "2000-07-01")),
    )

    assertThat(dto.toOffences(versionId)).isEmpty()
  }

  @Test
  fun `it should map offence type from the first ac eligible offence`() {
    val dto = monitoringOrderDto.copy(
      acEligibleOffences = mutableListOf(OffenceData(offence = "Robbery", offenceDate = "")),
    )

    assertThat(dto.toOffenceType()).isEqualTo("Robbery")
  }

  @Test
  fun `it should leave offence type null when there are no ac eligible offences`() {
    assertThat(monitoringOrderDto.toOffenceType()).isNull()
  }

  @Test
  fun `it should map monitoring conditions enum fields`() {
    val dto = monitoringOrderDto.copy(
      conditionType = "Bail Order",
      orderType = "Pre-Trial",
      orderTypeDescription = "DAPO",
      sentenceType = "Life Sentence",
      pilot = "Domestic Abuse Protection Order (DAPO)",
    )
    val conditions = dto.toMonitoringConditions(versionId)

    assertThat(conditions.versionId).isEqualTo(versionId)
    assertThat(conditions.conditionType).isEqualTo(MonitoringConditionType.BAIL_ORDER)
    assertThat(conditions.orderType).isEqualTo(OrderType.PRE_TRIAL)
    assertThat(conditions.orderTypeDescription).isEqualTo(OrderTypeDescription.DAPO)
    assertThat(conditions.pilot).isEqualTo(Pilot.DOMESTIC_ABUSE_PROTECTION_ORDER)
  }

  @Test
  fun `it should map issp and hdc Yes No strings to YesNoUnknown`() {
    val dto = monitoringOrderDto.copy(issp = "Yes", hdc = "No")
    val conditions = dto.toMonitoringConditions(versionId)

    assertThat(conditions.issp).isEqualTo(YesNoUnknown.YES)
    assertThat(conditions.hdc).isEqualTo(YesNoUnknown.NO)
  }

  @Test
  fun `it should map released under prarr true false strings to YesNoUnknown`() {
    val dto = monitoringOrderDto.copy(releasedUnderPrarr = "true")

    assertThat(dto.toMonitoringConditions(versionId).prarr).isEqualTo(YesNoUnknown.YES)
  }

  @Test
  fun `it should map dapol missed in error true string to YES`() {
    val dto = monitoringOrderDto.copy(dapolMissedInError = "true")

    assertThat(dto.toMonitoringConditions(versionId).dapolMissedInError).isEqualTo(YesNoUnknown.YES)
  }

  @Test
  fun `it should leave dapol missed in error null when blank`() {
    assertThat(monitoringOrderDto.toMonitoringConditions(versionId).dapolMissedInError).isNull()
  }

  @Test
  fun `it should map offence type onto monitoring conditions from ac eligible offences`() {
    val dto = monitoringOrderDto.copy(
      acEligibleOffences = mutableListOf(OffenceData(offence = "Robbery", offenceDate = "")),
    )

    assertThat(dto.toMonitoringConditions(versionId).offenceType).isEqualTo("Robbery")
  }

  @Test
  fun `it should leave monitoring condition enum fields null and not throw when unmatched`() {
    val conditions = monitoringOrderDto.toMonitoringConditions(versionId)

    assertThat(conditions.conditionType).isNull()
    assertThat(conditions.orderType).isNull()
    assertThat(conditions.orderTypeDescription).isNull()
    assertThat(conditions.sentenceType).isNull()
    assertThat(conditions.pilot).isNull()
  }

  @Test
  fun `it should map interested parties direct and matched fields`() {
    val dto = monitoringOrderDto.copy(
      responsibleOrganization = "Probation",
      responsibleOfficerName = "Jane Smith",
      responsibleOfficerEmail = "jane@example.com",
      responsibleOfficerPhone = "+441234567890",
      roEmail = "ro@example.com",
      notifyingOrganization = "Prison",
      noName = "HMP Example",
      noEmail = "no@example.com",
    )
    val parties = dto.toInterestedParties(versionId)

    assertThat(parties.versionId).isEqualTo(versionId)
    assertThat(parties.responsibleOrganisation).isEqualTo(ResponsibleOrganisation.PROBATION.name)
    assertThat(parties.responsibleOfficerName).isEqualTo("Jane Smith")
    assertThat(parties.responsibleOfficerEmail).isEqualTo("jane@example.com")
    assertThat(parties.responsibleOfficerPhoneNumber).isEqualTo("+441234567890")
    assertThat(parties.responsibleOrganisationEmail).isEqualTo("ro@example.com")
    assertThat(parties.notifyingOrganisation).isEqualTo(NotifyingOrganisationDDv5.PRISON.name)
    assertThat(parties.notifyingOrganisationName).isEqualTo("HMP Example")
    assertThat(parties.notifyingOrganisationEmail).isEqualTo("no@example.com")
  }

  @Test
  fun `it should map ro region as literal UKBA when notifying organisation is Home Office`() {
    val dto = monitoringOrderDto.copy(notifyingOrganization = "Home Office", roRegion = "doesn't matter")

    assertThat(dto.toInterestedParties(versionId).responsibleOrganisationRegion).isEqualTo("UKBA")
  }

  @Test
  fun `it should resolve ro region against the probation service region enum first`() {
    val dto = monitoringOrderDto.copy(roRegion = "London")

    assertThat(dto.toInterestedParties(versionId).responsibleOrganisationRegion)
      .isEqualTo(ProbationServiceRegion.LONDON.name)
  }

  @Test
  fun `it should fall back to youth justice service region when probation region does not match`() {
    val dto = monitoringOrderDto.copy(roRegion = "East and South East")

    assertThat(dto.toInterestedParties(versionId).responsibleOrganisationRegion)
      .isEqualTo(YouthJusticeServiceRegions.EAST_AND_SOUTH_EAST.name)
  }

  @Test
  fun `it should leave ro region null and not throw when unmatched`() {
    val dto = monitoringOrderDto.copy(roRegion = "not a real region")

    assertThat(dto.toInterestedParties(versionId).responsibleOrganisationRegion).isNull()
  }

  @Test
  fun `it should leave interested parties matched fields null and not throw when unmatched`() {
    val parties = monitoringOrderDto.toInterestedParties(versionId)

    assertThat(parties.responsibleOrganisation).isNull()
    assertThat(parties.notifyingOrganisation).isNull()
    assertThat(parties.responsibleOfficerName).isNull()
  }

  @Test
  fun `it should map dapo clause rows`() {
    val dto = monitoringOrderDto.copy(
      dapoOrderClauseNumbers = mutableListOf(DapoClause(dapoOrderClauseNumber = "Clause 1", date = "2000-07-01")),
    )
    val dapo = dto.toDapo(versionId)

    assertThat(dapo).hasSize(1)
    assertThat(dapo[0].versionId).isEqualTo(versionId)
    assertThat(dapo[0].clause).isEqualTo("Clause 1")
    assertThat(dapo[0].date).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 0, 0, 0, 0, ZoneId.of("Europe/London")),
    )
  }

  @Test
  fun `it should return no dapo rows when dapo order clause numbers is empty`() {
    assertThat(monitoringOrderDto.toDapo(versionId)).isEmpty()
  }

  @Test
  fun `it should map installation appointment date and place name`() {
    val dto = monitoringOrderDto.copy(
      dateAndTimeInstallationWillTakePlace = "2000-07-01 08:00:00",
      tagAtSourceDetails = "HMP Example",
    )
    val appointment = dto.toInstallationAppointment(versionId)

    assertThat(appointment).isNotNull
    assertThat(appointment!!.versionId).isEqualTo(versionId)
    assertThat(appointment.placeName).isEqualTo("HMP Example")
    assertThat(appointment.appointmentDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, ZoneId.of("Europe/London")),
    )
  }

  @Test
  fun `it should not build installation appointment when both date and place name are blank`() {
    assertThat(monitoringOrderDto.toInstallationAppointment(versionId)).isNull()
  }
}
