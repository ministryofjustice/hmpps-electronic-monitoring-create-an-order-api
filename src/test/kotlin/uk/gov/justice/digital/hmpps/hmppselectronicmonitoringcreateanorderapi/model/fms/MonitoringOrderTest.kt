package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.OrderTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.enums.ddv4.ProbationDeliveryUnits
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.enums.ddv5.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Zone

@ActiveProfiles("test")
class MonitoringOrderTest : OrderTestBase() {

  @Test
  fun `It should map attendance monitoring to an FMS Monitoring Order`() {
    val order = createOrder(
      mandatoryAttendanceConditions = listOf(
        createMandatoryAttendanceCondition(),
      ),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order = order, caseId = "")

    assertThat(fmsMonitoringOrder.inclusionZones).isEqualTo(
      listOf(
        Zone(
          description = order.mandatoryAttendanceConditions[0].purpose + "\n" +
            order.mandatoryAttendanceConditions[0].appointmentDay + " " +
            order.mandatoryAttendanceConditions[0].startTime + "-" +
            order.mandatoryAttendanceConditions[0].endTime + "\n" +
            order.mandatoryAttendanceConditions[0].addressLine1 + "\n" +
            order.mandatoryAttendanceConditions[0].addressLine2 + "\n" +
            order.mandatoryAttendanceConditions[0].addressLine3 + "\n" +
            order.mandatoryAttendanceConditions[0].addressLine4 + "\n" +
            order.mandatoryAttendanceConditions[0].postcode + "\n",
          duration = "",
          start = "2025-01-01",
          end = "2025-02-01",
        ),
      ),
    )
    assertThat(fmsMonitoringOrder.enforceableCondition).contains(
      EnforceableCondition(
        condition = "Attendance Requirement",
        startDate = "2025-01-01 01:01:01",
        endDate = "2025-02-01 01:01:01",
      ),
    )
  }

  @ParameterizedTest(name = "it should map probation delivery unit to Serco - {0} -> {1}")
  @EnumSource(ProbationDeliveryUnits::class)
  fun `It should map correctly map saved probation delivery unit values to Serco`(pdu: ProbationDeliveryUnits) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(),
      installationAndRisk = createInstallationAndRisk(riskCategory = pdu.name),
      interestedParties = createInterestedParty(responsibleOrganisation = "PROBATION"),
      probationDeliveryUnits = createProbationDeliveryUnit(pdu.name),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.pduResponsible).isEqualTo(pdu.value)
  }

  @ParameterizedTest(name = "it should map prison - {0} -> {1}")
  @EnumSource(Prison::class)
  fun `It should correctly map saved prison values to Serco`(prison: Prison) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(),
      interestedParties = createInterestedParty(
        responsibleOrganisation = "PROBATION",
        notifyingOrganisationName = prison.name,
      ),

    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.noName).isEqualTo(prison.value)
  }

  @ParameterizedTest(name = "it should map pilot to Serco - {0} -> {1}")
  @EnumSource(Pilot::class)
  fun `It should correctly map saved pilot values to Serco`(pilot: Pilot) {
    val order = createOrder(
      monitoringConditions = createMonitoringConditions(pilot = Pilot.entries.first { it.name == pilot.name }),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.pilot).isEqualTo(pilot.value)
  }

  @ParameterizedTest(name = "it should map alcohol condition types - {0} -> {1}")
  @MethodSource("getAlcoholNotifiyingOrganisations")
  fun `It should map correctly map alcohol condition types to Serco`(
    savedValue: NotifyingOrganisationDDv5,
    mappedValue: String,
  ) {
    val order = createOrder(
      interestedParties = createInterestedParty(notifyingOrganisation = savedValue.value),

      monitoringConditions = createMonitoringConditions(alcohol = true),
      alcoholMonitoringConditions = createAlcoholMonitoringConditions(
        monitoringType = AlcoholMonitoringType.ALCOHOL_LEVEL,
      ),
    )

    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, "")

    assertThat(fmsMonitoringOrder.enforceableCondition!!.first().condition).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map alcohol abstinence - {0} -> {1}")
  @MethodSource("getAlcoholAbstinence")
  fun `It should map correctly map alcohol abstinence to Serco`(
    savedValue: AlcoholMonitoringType,
    mappedValue: String,
  ) {
    NotifyingOrganisationDDv5.entries.forEach {
      val order = createOrder(
        interestedParties = createInterestedParty(notifyingOrganisation = it.value),

        monitoringConditions = createMonitoringConditions(alcohol = true),
        alcoholMonitoringConditions = createAlcoholMonitoringConditions(
          monitoringType = savedValue,
        ),
      )

      val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, "")
      assertThat(fmsMonitoringOrder.abstinence).isEqualTo(mappedValue)
    }
  }

  companion object {
    @JvmStatic
    fun getAlcoholNotifiyingOrganisations() = listOf(
      Arguments.of(NotifyingOrganisationDDv5.CIVIL_COUNTY_COURT, "AAMR"),
      Arguments.of(NotifyingOrganisationDDv5.CROWN_COURT, "AAMR"),
      Arguments.of(NotifyingOrganisationDDv5.MAGISTRATES_COURT, "AAMR"),
      Arguments.of(NotifyingOrganisationDDv5.MILITARY_COURT, "AAMR"),
      Arguments.of(NotifyingOrganisationDDv5.PRISON, "AML"),
      Arguments.of(NotifyingOrganisationDDv5.HOME_OFFICE, "AAMR"),
      Arguments.of(NotifyingOrganisationDDv5.SCOTTISH_COURT, "AAMR"),
      Arguments.of(NotifyingOrganisationDDv5.FAMILY_COURT, "AAMR"),
      Arguments.of(NotifyingOrganisationDDv5.PROBATION, "AML"),
      Arguments.of(NotifyingOrganisationDDv5.YOUTH_COURT, "AAMR"),
      Arguments.of(NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE, "AAMR"),
    )

    @JvmStatic
    fun getAlcoholAbstinence() = listOf(
      Arguments.of(AlcoholMonitoringType.ALCOHOL_ABSTINENCE, "Yes"),
      Arguments.of(AlcoholMonitoringType.ALCOHOL_LEVEL, "No"),

    )
  }
}
