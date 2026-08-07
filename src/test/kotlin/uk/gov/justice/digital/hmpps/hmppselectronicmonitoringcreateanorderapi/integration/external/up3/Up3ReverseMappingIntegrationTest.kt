package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.external.up3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RiskCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.Up3RetrieveDWandMO
import java.nio.file.Files
import java.nio.file.Paths
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class Up3ReverseMappingIntegrationTest {

  private val objectMapper = jacksonObjectMapper()
  private val versionId: UUID = UUID.randomUUID()
  private val london: ZoneId = ZoneId.of("Europe/London")

  private val response: Up3RetrieveDWandMO = objectMapper.readValue(
    Files.readString(Paths.get("src/test/resources/json/up3/retrieveDWandMO_example.json")),
  )

  @Test
  fun `it should deserialize the example payload`() {
    assertThat(response.caseId).isEqualTo("16542c2e83714b10b2409870ceaad324")
  }

  @Test
  fun `it should map the device wearer`() {
    val deviceWearer = response.deviceWearer.toDeviceWearer(versionId)

    assertThat(deviceWearer.firstName).isEqualTo("Bob")
    assertThat(deviceWearer.middleName).isEqualTo("Middleton")
    assertThat(deviceWearer.lastName).isEqualTo("Smith")
    assertThat(deviceWearer.alias).isEqualTo("Robert")
    assertThat(deviceWearer.dateOfBirth).isEqualTo(ZonedDateTime.of(2000, 7, 1, 0, 0, 0, 0, london))
    assertThat(deviceWearer.adultAtTimeOfInstallation).isTrue()
    assertThat(deviceWearer.sex).isEqualTo("Male")
    assertThat(deviceWearer.gender).isEqualTo("Male")
    assertThat(deviceWearer.language).isEqualTo("English")
    assertThat(deviceWearer.interpreterRequired).isFalse()
    assertThat(deviceWearer.pncId).isEqualTo("pnc123")
    assertThat(deviceWearer.nomisId).isEqualTo("nomis123")
    assertThat(deviceWearer.deliusId).isEqualTo("delius123")
    assertThat(deviceWearer.prisonNumber).isEqualTo("prison123")
    assertThat(deviceWearer.homeOfficeReferenceNumber).isEqualTo("hocrn123")
    assertThat(deviceWearer.complianceAndEnforcementPersonReference).isEqualTo("cepr123")
    assertThat(deviceWearer.disabilities).isEqualTo("${Disability.VISION.name},${Disability.HEARING.name}")
  }

  @Test
  fun `it should map primary, secondary and tertiary addresses`() {
    val primary = response.deviceWearer.toAddress(versionId)
    val secondary = response.deviceWearer.toSecondaryAddress(versionId)
    val tertiary = response.deviceWearer.toTertiaryAddress(versionId)

    assertThat(primary!!.addressType).isEqualTo(AddressType.PRIMARY)
    assertThat(primary.addressLine1).isEqualTo("1 Fake Street")
    assertThat(primary.postcode).isEqualTo("AB1 2CD")

    assertThat(secondary!!.addressType).isEqualTo(AddressType.SECONDARY)
    assertThat(secondary.addressLine1).isEqualTo("2 Fake Street")
    assertThat(secondary.postcode).isEqualTo("AB2 3CD")

    assertThat(tertiary).isNull()
  }

  @Test
  fun `it should map mappa, installation and risk, and responsible adult`() {
    val mappa = response.deviceWearer.toMappa(versionId)
    val installationAndRisk = response.deviceWearer.toInstallationAndRisk(versionId)
    val responsibleAdult = response.deviceWearer.toResponsibleAdult(versionId)

    assertThat(mappa.level).isEqualTo(MappaLevel.MAPPA_TWO)
    assertThat(mappa.category).isEqualTo(MappaCategory.CATEGORY_ONE)
    assertThat(mappa.isMappa).isEqualTo(YesNoUnknown.YES)

    assertThat(installationAndRisk.riskDetails).isEqualTo("known to be aggressive")
    assertThat(installationAndRisk.riskCategory).containsExactlyInAnyOrder(
      RiskCategory.SEXUAL_OFFENCES.name,
      RiskCategory.RACIAL_ABUSE_OR_THREATS.name,
    )

    assertThat(responsibleAdult!!.fullName).isEqualTo("Jane Smith")
    assertThat(responsibleAdult.relationship).isEqualTo("Parent")
    assertThat(responsibleAdult.contactNumber).isEqualTo("07000000000")
  }

  @Test
  fun `it should map curfew conditions, release conditions and timetable`() {
    val order = response.monitoringOrder

    val curfew = order.toCurfewConditions(versionId)
    assertThat(curfew!!.startDate).isEqualTo(ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, london))
    assertThat(curfew.endDate).isEqualTo(ZonedDateTime.of(2001, 7, 1, 8, 0, 0, 0, london))
    assertThat(curfew.curfewAdditionalDetails).isEqualTo("wears a tag")

    val release = order.toCurfewReleaseDateConditions(versionId)
    assertThat(release!!.releaseDate).isEqualTo(ZonedDateTime.of(2000, 7, 1, 0, 0, 0, 0, london))
    assertThat(release.startTime).isEqualTo("08:00")
    assertThat(release.endTime).isEqualTo("09:00")

    val timetable = order.toCurfewTimeTable(versionId)
    assertThat(timetable).hasSize(2)
    assertThat(timetable[0].dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
    assertThat(timetable[0].curfewAddress).isEqualTo("PRIMARY_ADDRESS")
    assertThat(timetable[1].dayOfWeek).isEqualTo(DayOfWeek.WEDNESDAY)
  }

  @Test
  fun `it should map trail and alcohol monitoring conditions`() {
    val order = response.monitoringOrder

    val trail = order.toTrailMonitoringConditions(versionId)
    assertThat(trail!!.deviceType?.name).isEqualTo("FITTED")

    val alcohol = order.toAlcoholMonitoringConditions(versionId)
    assertThat(alcohol!!.monitoringType).isEqualTo(AlcoholMonitoringType.ALCOHOL_ABSTINENCE)
  }

  @Test
  fun `it should map exclusion and inclusion zones with a contiguous zoneId`() {
    val zones = response.monitoringOrder.toEnforcementZoneConditions(versionId)

    assertThat(zones).hasSize(2)
    assertThat(zones[0].description).isEqualTo("no go area")
    assertThat(zones[0].zoneId).isEqualTo(0)
    assertThat(zones[1].description).isEqualTo("must stay here")
    assertThat(zones[1].zoneId).isEqualTo(1)
  }

  @Test
  fun `it should map variation details, order id, order version type and court case reference`() {
    val order = response.monitoringOrder

    val variation = order.toVariationDetails(versionId)
    assertThat(variation!!.variationDetails).isEqualTo("changed curfew hours")

    assertThat(order.toOrderId()).isEqualTo(UUID.fromString("b6e8c6c2-4f1a-4b5b-9f3e-1a2b3c4d5e6f"))
    assertThat(order.toCourtCaseReferenceNumber()).isEqualTo("CCRN123")
  }

  @Test
  fun `it should map probation delivery unit, offences and offence type`() {
    val order = response.monitoringOrder

    val pdu = order.toProbationDeliveryUnit(versionId)
    assertThat(pdu).isNotNull()

    val installationAndRisk = order.toInstallationAndRisk(versionId)
    assertThat(installationAndRisk!!.offence).isEqualTo("ROBBERY")

    val offences = order.toOffences(versionId)
    assertThat(offences).hasSize(1)
    assertThat(offences[0].offenceType).isEqualTo("ROBBERY")

    assertThat(order.toOffenceType()).isEqualTo("Robbery")
  }

  @Test
  fun `it should map monitoring conditions`() {
    val conditions = response.monitoringOrder.toMonitoringConditions(versionId)

    assertThat(conditions.conditionType).isEqualTo(MonitoringConditionType.BAIL_ORDER)
    assertThat(conditions.orderType).isEqualTo(OrderType.PRE_TRIAL)
    assertThat(conditions.orderTypeDescription).isEqualTo(OrderTypeDescription.DAPO)
    assertThat(conditions.pilot).isEqualTo(Pilot.DOMESTIC_ABUSE_PROTECTION_ORDER)
    assertThat(conditions.issp).isEqualTo(YesNoUnknown.YES)
    assertThat(conditions.hdc).isEqualTo(YesNoUnknown.NO)
    assertThat(conditions.prarr).isEqualTo(YesNoUnknown.YES)
    assertThat(conditions.dapolMissedInError).isEqualTo(YesNoUnknown.YES)
    assertThat(conditions.offenceType).isEqualTo("Robbery")
  }

  @Test
  fun `it should map interested parties`() {
    val parties = response.monitoringOrder.toInterestedParties(versionId)

    assertThat(parties.responsibleOrganisation).isEqualTo(ResponsibleOrganisation.PROBATION.name)
    assertThat(parties.responsibleOfficerName).isEqualTo("Jane Smith")
    assertThat(parties.responsibleOfficerEmail).isEqualTo("jane@example.com")
    assertThat(parties.responsibleOfficerPhoneNumber).isEqualTo("+441234567890")
    assertThat(parties.responsibleOrganisationEmail).isEqualTo("ro@example.com")
    assertThat(parties.responsibleOrganisationRegion).isEqualTo("LONDON")
    assertThat(parties.notifyingOrganisation).isEqualTo(NotifyingOrganisationDDv5.PRISON.name)
    assertThat(parties.notifyingOrganisationName).isEqualTo("HMP Example")
    assertThat(parties.notifyingOrganisationEmail).isEqualTo("no@example.com")
  }

  @Test
  fun `it should map dapo clauses and the installation appointment`() {
    val order = response.monitoringOrder

    val dapo = order.toDapo(versionId)
    assertThat(dapo).hasSize(1)
    assertThat(dapo[0].clause).isEqualTo("Clause 1")
    assertThat(dapo[0].date).isEqualTo(ZonedDateTime.of(2000, 7, 1, 0, 0, 0, 0, london))

    val appointment = order.toInstallationAppointment(versionId)
    assertThat(appointment!!.placeName).isEqualTo("HMP Example")
    assertThat(appointment.appointmentDate).isEqualTo(ZonedDateTime.of(2000, 7, 1, 8, 0, 0, 0, london))
  }
}
