package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.external.up3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.Up3CurfewDuration
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.Up3CurfewSchedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.Up3EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.Up3MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.Up3Zone
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class Up3MonitoringOrderTest {

  val up3MonitoringOrder =
    Up3MonitoringOrder(
      orderId = "order-1",
      orderRequestType = "New Order",
      orderType = "Standalone",
      curfewStart = "",
      curfewEnd = "",
      curfewDescription = "",
      conditionalReleaseDate = "",
      conditionalReleaseStartTime = "",
      conditionalReleaseEndTime = "",
      curfewDuration = emptyList(),
      enforceableCondition = emptyList(),
      abstinence = "",
      orderVariationDate = "",
      orderVariationDetails = "",
      exclusionZones = emptyList(),
      inclusionZones = emptyList(),
    )
  val versionId: UUID = UUID.randomUUID()

  @Test
  fun `it should map curfew conditions when curfew start is present`() {
    val up3 = up3MonitoringOrder.copy(
      curfewStart = "2000-07-01 08:00:00",
      curfewEnd = "2001-07-01 08:00:00",
      curfewDescription = "wears a tag",
    )
    val curfew = up3.toCurfewConditions(versionId)

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
    assertThat(up3MonitoringOrder.toCurfewConditions(versionId)).isNull()
  }

  @Test
  fun `it should map curfew release date conditions when release date is present`() {
    val up3 = up3MonitoringOrder.copy(
      conditionalReleaseDate = "2000-07-01",
      conditionalReleaseStartTime = "08:00",
      conditionalReleaseEndTime = "09:00",
    )
    val releaseConditions = up3.toCurfewReleaseDateConditions(versionId)

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
    assertThat(up3MonitoringOrder.toCurfewReleaseDateConditions(versionId)).isNull()
  }

  @Test
  fun `it should map curfew timetable rows for each schedule entry`() {
    val up3 = up3MonitoringOrder.copy(
      curfewDuration = listOf(
        Up3CurfewDuration(
          location = "primary",
          allday = "",
          schedule = listOf(
            Up3CurfewSchedule(day = "Mo", start = "08:00", end = "09:00"),
            Up3CurfewSchedule(day = "Wed", start = "10:00", end = "11:00"),
          ),
        ),
      ),
    )
    val rows = up3.toCurfewTimeTable(versionId)

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
    val up3 = up3MonitoringOrder.copy(
      curfewDuration = listOf(
        Up3CurfewDuration("secondary", "", listOf(Up3CurfewSchedule("Tu", "08:00", "09:00"))),
        Up3CurfewDuration("tertiary", "", listOf(Up3CurfewSchedule("Th", "08:00", "09:00"))),
      ),
    )
    val rows = up3.toCurfewTimeTable(versionId)

    assertThat(rows).hasSize(2)
    assertThat(rows[0].curfewAddress).isEqualTo("SECONDARY_ADDRESS")
    assertThat(rows[0].dayOfWeek).isEqualTo(DayOfWeek.TUESDAY)
    assertThat(rows[1].curfewAddress).isEqualTo("TERTIARY_ADDRESS")
    assertThat(rows[1].dayOfWeek).isEqualTo(DayOfWeek.THURSDAY)
  }

  @Test
  fun `it should drop unmatched day codes and not throw`() {
    val up3 = up3MonitoringOrder.copy(
      curfewDuration = listOf(
        Up3CurfewDuration(
          "primary",
          "",
          listOf(
            Up3CurfewSchedule("Mo", "08:00", "09:00"),
            Up3CurfewSchedule("not a real day", "08:00", "09:00"),
          ),
        ),
      ),
    )
    val rows = up3.toCurfewTimeTable(versionId)

    assertThat(rows).hasSize(1)
    assertThat(rows[0].dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
  }

  @Test
  fun `it should return no curfew timetable rows when curfew duration is empty`() {
    assertThat(up3MonitoringOrder.toCurfewTimeTable(versionId)).isEmpty()
  }

  @Test
  fun `it should map trail monitoring conditions for a fitted device`() {
    val up3 = up3MonitoringOrder.copy(
      enforceableCondition = listOf(
        Up3EnforceableCondition(
          "Location Monitoring (Fitted Device)",
          "2000-07-01 08:00:00",
          "2001-07-01 08:00:00",
        ),
      ),
    )
    val trail = up3.toTrailMonitoringConditions(versionId)

    assertThat(trail).isNotNull
    assertThat(trail!!.versionId).isEqualTo(versionId)
    assertThat(trail.deviceType).isEqualTo(DeviceType.FITTED)
    assertThat(trail.startDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, ZoneId.of("Europe/London")),
    )
  }

  @Test
  fun `it should map trail monitoring conditions for a non-fitted device`() {
    val up3 = up3MonitoringOrder.copy(
      enforceableCondition = listOf(
        Up3EnforceableCondition(
          "Location Monitoring (using Non-Fitted Device)",
          "2000-07-01 08:00:00",
          "2001-07-01 08:00:00",
        ),
      ),
    )
    val trail = up3.toTrailMonitoringConditions(versionId)

    assertThat(trail!!.deviceType).isEqualTo(DeviceType.NON_FITTED)
  }

  @Test
  fun `it should not build trail monitoring conditions when no matching condition is present`() {
    assertThat(up3MonitoringOrder.toTrailMonitoringConditions(versionId)).isNull()
  }

  @Test
  fun `it should map alcohol monitoring conditions when an AML condition is present`() {
    val up3 = up3MonitoringOrder.copy(
      abstinence = "Yes",
      enforceableCondition = listOf(
        Up3EnforceableCondition("AML", "2000-07-01 08:00:00", "2001-07-01 08:00:00"),
      ),
    )
    val alcohol = up3.toAlcoholMonitoringConditions(versionId)

    assertThat(alcohol).isNotNull
    assertThat(alcohol!!.versionId).isEqualTo(versionId)
    assertThat(alcohol.monitoringType).isEqualTo(AlcoholMonitoringType.ALCOHOL_ABSTINENCE)
    assertThat(alcohol.startDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, ZoneId.of("Europe/London")),
    )
  }

  @Test
  fun `it should map alcohol monitoring conditions when an AAMR condition is present`() {
    val up3 = up3MonitoringOrder.copy(
      abstinence = "No",
      enforceableCondition = listOf(
        Up3EnforceableCondition("AAMR", "2000-07-01 08:00:00", "2001-07-01 08:00:00"),
      ),
    )
    val alcohol = up3.toAlcoholMonitoringConditions(versionId)

    assertThat(alcohol).isNotNull
    assertThat(alcohol!!.monitoringType).isEqualTo(AlcoholMonitoringType.ALCOHOL_LEVEL)
  }

  @Test
  fun `it should not build alcohol monitoring conditions when no matching condition is present`() {
    assertThat(up3MonitoringOrder.toAlcoholMonitoringConditions(versionId)).isNull()
  }

  @Test
  fun `it should map exclusion zones with zoneType EXCLUSION`() {
    val up3 = up3MonitoringOrder.copy(
      exclusionZones = listOf(
        Up3Zone(description = "no go area", duration = "2 weeks", start = "2000-07-01", end = "2001-07-01"),
      ),
    )
    val rows = up3.toEnforcementZoneConditions(versionId)

    assertThat(rows).hasSize(1)
    assertThat(rows[0].versionId).isEqualTo(versionId)
    assertThat(rows[0].zoneType).isEqualTo(EnforcementZoneType.EXCLUSION)
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
    val up3 = up3MonitoringOrder.copy(
      inclusionZones = listOf(
        Up3Zone(description = "must stay here", duration = "1 week", start = "2000-07-01", end = "2001-07-01"),
      ),
    )
    val rows = up3.toEnforcementZoneConditions(versionId)

    assertThat(rows).hasSize(1)
    assertThat(rows[0].zoneType).isEqualTo(EnforcementZoneType.INCLUSION)
    assertThat(rows[0].description).isEqualTo("must stay here")
    assertThat(rows[0].duration).isEqualTo("1 week")
  }

  @Test
  fun `it should map exclusion and inclusion zones together, exclusion rows first`() {
    val up3 = up3MonitoringOrder.copy(
      exclusionZones = listOf(Up3Zone("exclusion 1", "", "2000-07-01", "")),
      inclusionZones = listOf(Up3Zone("inclusion 1", "", "2000-07-01", "")),
    )
    val rows = up3.toEnforcementZoneConditions(versionId)

    assertThat(rows).hasSize(2)
    assertThat(rows[0].zoneType).isEqualTo(EnforcementZoneType.EXCLUSION)
    assertThat(rows[1].zoneType).isEqualTo(EnforcementZoneType.INCLUSION)
  }

  @Test
  fun `it should leave end date null when zone end is blank`() {
    val up3 = up3MonitoringOrder.copy(
      exclusionZones = listOf(Up3Zone("no go area", "", "2000-07-01", "")),
    )
    val rows = up3.toEnforcementZoneConditions(versionId)

    assertThat(rows[0].endDate).isNull()
  }

  @Test
  fun `it should return no enforcement zone rows when no zones are present`() {
    assertThat(up3MonitoringOrder.toEnforcementZoneConditions(versionId)).isEmpty()
  }

  @Test
  fun `it should map variation details when order variation date is present`() {
    val up3 = up3MonitoringOrder.copy(
      orderVariationDate = "2000-07-01 08:00:00",
      orderVariationDetails = "changed curfew hours",
    )
    val variation = up3.toVariationDetails(versionId)

    assertThat(variation).isNotNull
    assertThat(variation!!.versionId).isEqualTo(versionId)
    assertThat(variation.variationDate).isEqualTo(
      ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, ZoneId.of("Europe/London")),
    )
    assertThat(variation.variationDetails).isEqualTo("changed curfew hours")
  }

  @Test
  fun `it should not build variation details when order variation date is blank`() {
    assertThat(up3MonitoringOrder.toVariationDetails(versionId)).isNull()
  }
}
