package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.external.cvl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence.Licence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence.LicenceConditionMapper
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class LicenceConditionMapperTest {
  private val licenceConditions =
    this::class.java.getResource("/fixtures/cvl/licenceConditions.json")!!.readText()

  private val versionId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

  @Test
  fun `maps to curfew monitoring conditions`() {
    val licence = jacksonObjectMapper().readValue<Licence>(licenceConditions)
    val result = LicenceConditionMapper().map(licence, versionId)
    val monitoringConditions = result.filterIsInstance<CurfewConditions>()

    assertThat(monitoringConditions).hasSize(1)
    val monitoringCondition = monitoringConditions.single()

    assertThat(monitoringCondition.versionId.toString() == versionId.toString())
    assertThat(monitoringCondition.startDate == LocalDate.parse("2026-12-01").atStartOfDay(ZoneId.of("Europe/London")))
    assertThat(monitoringCondition.endDate == null)
    assertThat(
      monitoringCondition.curfewDescription.toString() ==
        "You must stay at 123 Test Street, Test, T1234TT between 5pm and midnight every day until your electronic tag is installed unless otherwise authorised by your supervising officer.",
    )
  }

  @Test
  fun `maps to trail monitoring conditions`() {
    val licence = jacksonObjectMapper().readValue<Licence>(licenceConditions)
    val result = LicenceConditionMapper().map(licence, versionId)
    val monitoringConditions = result.filterIsInstance<TrailMonitoringConditions>()

    assertThat(monitoringConditions).hasSize(1)
    val monitoringCondition = monitoringConditions.single()

    assertThat(monitoringCondition.versionId.toString() == versionId.toString())
    assertThat(monitoringCondition.startDate == LocalDate.parse("2026-12-01").atStartOfDay(ZoneId.of("Europe/London")))
    assertThat(monitoringCondition.endDate == LocalDate.parse("2027-12-01").atStartOfDay(ZoneId.of("Europe/London")))
  }

  @Test
  fun `maps to alcohol level and abstinence monitoring conditions`() {
    val licence = jacksonObjectMapper().readValue<Licence>(licenceConditions)
    val result = LicenceConditionMapper().map(licence, versionId)
    val monitoringConditions = result.filterIsInstance<AlcoholMonitoringConditions>()

    assertThat(monitoringConditions).hasSize(2)

    val byType = monitoringConditions.associateBy { it.monitoringType }

    assertThat(byType.keys).containsExactlyInAnyOrder(
      AlcoholMonitoringType.ALCOHOL_LEVEL,
      AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
    )
    assertThat(byType.getValue(AlcoholMonitoringType.ALCOHOL_LEVEL).id.toString() == versionId.toString())
    assertThat(
      byType.getValue(AlcoholMonitoringType.ALCOHOL_LEVEL).startDate ==
        LocalDate.parse("2026-12-01").atStartOfDay(ZoneId.of("Europe/London")),
    )
    assertThat(
      byType.getValue(AlcoholMonitoringType.ALCOHOL_LEVEL).endDate,
    ).isEqualTo(LocalDate.parse("2027-02-05").atStartOfDay(ZoneId.of("Europe/London")))
    assertThat(byType.getValue(AlcoholMonitoringType.ALCOHOL_ABSTINENCE).id.toString() == versionId.toString())
    assertThat(
      byType.getValue(AlcoholMonitoringType.ALCOHOL_ABSTINENCE).startDate ==
        LocalDate.parse("2026-12-01").atStartOfDay(ZoneId.of("Europe/London")),
    )
    assertThat(byType.getValue(AlcoholMonitoringType.ALCOHOL_ABSTINENCE).endDate).isNull()
  }

  @Test
  fun `maps restriction to enforcement monitoring conditions`() {
    val licence = jacksonObjectMapper().readValue<Licence>(licenceConditions)
    val result = LicenceConditionMapper().map(licence, versionId)
    val monitoringConditions = result.filterIsInstance<EnforcementZoneConditions>()

    assertThat(monitoringConditions).hasSize(1)
    val monitoringCondition = monitoringConditions.single()

    assertThat(monitoringCondition.versionId.toString() == versionId.toString())
    assertThat(monitoringCondition.startDate == LocalDate.parse("2026-12-01").atStartOfDay(ZoneId.of("Europe/London")))
    assertThat(monitoringCondition.endDate == null)
  }

  @Test
  fun `maps exclusion with attachment to enforcement monitoring conditions`() {
    val licence = jacksonObjectMapper().readValue<Licence>(licenceConditions)
    val result = LicenceConditionMapper().map(licence, versionId)
    val monitoringConditions = result.filterIsInstance<EnforcementZoneConditions>()

    assertThat(monitoringConditions).hasSize(1)
    val monitoringCondition = monitoringConditions.single()

    assertThat(monitoringCondition.versionId.toString() == versionId.toString())
    assertThat(monitoringCondition.startDate == LocalDate.parse("2026-12-01").atStartOfDay(ZoneId.of("Europe/London")))
    assertThat(monitoringCondition.endDate == null)
  }

  @Test
  fun `maps exclusion with text to enforcement monitoring conditions`() {
    val licence = jacksonObjectMapper().readValue<Licence>(licenceConditions)
    val result = LicenceConditionMapper().map(licence, versionId)
    val monitoringConditions = result.filterIsInstance<EnforcementZoneConditions>()

    assertThat(monitoringConditions).hasSize(1)
    val monitoringCondition = monitoringConditions.single()

    assertThat(monitoringCondition.versionId.toString() == versionId.toString())
    assertThat(monitoringCondition.startDate == LocalDate.parse("2026-12-01").atStartOfDay(ZoneId.of("Europe/London")))
    assertThat(monitoringCondition.endDate == null)
  }
}
