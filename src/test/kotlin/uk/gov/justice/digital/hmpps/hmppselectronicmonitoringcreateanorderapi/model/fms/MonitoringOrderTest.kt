package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.OrderTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.AlcoholAbstinenceArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.AlcoholNotifyingOrganisationArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.CivilAndCountyCourtArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.CrownCourtArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.FamilyCourtArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MagistratesCourtArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MilitaryCourtArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.NotifyingOrganisationArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.OrderTypeArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.PilotArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.PrisonArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.ProbationDeliveryUnitArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.ProbationDeliveryUnitDDv6ArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.SentenceArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.YouthCourtArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SentenceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Zone
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

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

  @Test
  fun `It should map curfew timeable for primary address to an FMS Monitoring Order`() {
    val startTime = "19:00:00"
    val endTime = "07:00:00"
    val primaryAddress = createAddress(
      addressLine1 = "Primary Line 1",
      addressLine2 = "Primary Line 2",
      addressLine3 = "Primary Line 3",
      addressLine4 = "Primary Line 4",
      postcode = "Primary Post code",
      addressType = AddressType.PRIMARY,
    )

    val order = createOrder(
      addresses = mutableListOf(primaryAddress),
      monitoringConditions = createMonitoringConditions(curfew = true),
      curfewTimetable = createCurfewTimeTable(
        startTime = startTime,
        endTime = endTime,
        curfewAddress = "PRIMARY_ADDRESS",
      ),

    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)
    val days = listOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")

    val primaryAddressCurfew = fmsMonitoringOrder.curfewDuration!!.firstOrNull { it.location == "primary" }
    assertThat(primaryAddressCurfew).isNotNull
    days.forEach { it ->
      val day = primaryAddressCurfew?.schedule?.firstOrNull { schedule -> schedule.day == it }
      assertThat(day).isNotNull
      assertThat(day!!.start).isEqualTo(startTime)
      assertThat(day.end).isEqualTo(endTime)
    }
  }

  @Test
  fun `It should map curfew timeable for secondary address to an FMS Monitoring Order`() {
    val startTime = "19:00:00"
    val endTime = "07:00:00"
    val primaryAddress = createAddress(
      addressLine1 = "Primary Line 1",
      addressLine2 = "Primary Line 2",
      addressLine3 = "Primary Line 3",
      addressLine4 = "Primary Line 4",
      postcode = "Primary Post code",
      addressType = AddressType.PRIMARY,
    )

    val mockAddress = createAddress(
      addressLine1 = "TERTIARY Line 1",
      addressLine2 = "TERTIARY Line 2",
      addressLine3 = "TERTIARY Line 3",
      addressLine4 = "TERTIARY Line 4",
      postcode = "TERTIARY Post code",
      addressType = AddressType.SECONDARY,
    )
    val order = createOrder(
      addresses = mutableListOf(primaryAddress, mockAddress),
      monitoringConditions = createMonitoringConditions(curfew = true),
      curfewTimetable = createCurfewTimeTable(
        startTime = startTime,
        endTime = endTime,
        curfewAddress = "SECONDARY_ADDRESS",
      ),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)
    val days = listOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")

    val primaryAddressCurfew = fmsMonitoringOrder.curfewDuration!!.firstOrNull { it.location == "secondary" }
    assertThat(primaryAddressCurfew).isNotNull
    days.forEach { it ->
      val day = primaryAddressCurfew?.schedule?.firstOrNull { schedule -> schedule.day == it }
      assertThat(day).isNotNull
      assertThat(day!!.start).isEqualTo(startTime)
      assertThat(day.end).isEqualTo(endTime)
    }
  }

  @Test
  fun `It should map curfew timeable for tertiary address to an FMS Monitoring Order`() {
    val startTime = "19:00:00"
    val endTime = "07:00:00"
    val primaryAddress = createAddress(
      addressLine1 = "Primary Line 1",
      addressLine2 = "Primary Line 2",
      addressLine3 = "Primary Line 3",
      addressLine4 = "Primary Line 4",
      postcode = "Primary Post code",
      addressType = AddressType.PRIMARY,
    )
    val mockAddress = createAddress(
      addressLine1 = "TERTIARY Line 1",
      addressLine2 = "TERTIARY Line 2",
      addressLine3 = "TERTIARY Line 3",
      addressLine4 = "TERTIARY Line 4",
      postcode = "TERTIARY Post code",
      addressType = AddressType.TERTIARY,
    )
    val order = createOrder(
      addresses = mutableListOf(primaryAddress, mockAddress),
      monitoringConditions = createMonitoringConditions(curfew = true),
      curfewTimetable = createCurfewTimeTable(
        startTime = startTime,
        endTime = endTime,
        curfewAddress = "TERTIARY_ADDRESS",
      ),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)
    val days = listOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")

    val primaryAddressCurfew = fmsMonitoringOrder.curfewDuration!!.firstOrNull { it.location == "tertiary" }
    assertThat(primaryAddressCurfew).isNotNull
    days.forEach { it ->
      val day = primaryAddressCurfew?.schedule?.firstOrNull { schedule -> schedule.day == it }
      assertThat(day).isNotNull
      assertThat(day!!.start).isEqualTo(startTime)
      assertThat(day.end).isEqualTo(endTime)
    }
  }

  @Test
  fun `It should map offence additional details to an FMS Monitoring Order`() {
    val order = createOrder(
      installationAndRisk = createInstallationAndRisk(
        offenceAdditionalDetails = "Mock Additional Details",
      ),
      monitoringConditions = createMonitoringConditions(
        offenceType = "Robbery",
        policeArea = "Avon and Somerset",
      ),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(
      fmsMonitoringOrder.offenceAdditionalDetails,
    ).isEqualTo(
      "Mock Additional Details. AC Offence: Robbery. PFA: Avon and Somerset",
    )
  }

  @Test
  fun `It should map the police area correctly`() {
    val order = createOrder(
      installationAndRisk = createInstallationAndRisk(
        offenceAdditionalDetails = "Mock Additional Details",
      ),
      monitoringConditions = createMonitoringConditions(
        policeArea = "AVON_AND_SOMERSET",
      ),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(
      fmsMonitoringOrder.offenceAdditionalDetails,
    ).isEqualTo(
      "Mock Additional Details. PFA: Avon and Somerset",
    )
  }

  @Test
  fun `It should map curfew day of release to an FMS Monitoring Order`() {
    val startTime = "19:00:00"
    val endTime = "07:00:00"
    val primaryAddress = createAddress(
      addressLine1 = "Primary Line 1",
      addressLine2 = "Primary Line 2",
      addressLine3 = "Primary Line 3",
      addressLine4 = "Primary Line 4",
      postcode = "Primary Post code",
      addressType = AddressType.PRIMARY,
    )
    val mockeDayOfRelease = createCurfewDayOfReslse(
      startTime = "20:00:00",
      endTime = "08:00:00",
      releaseDate = ZonedDateTime.now(),
    )
    val order = createOrder(
      addresses = mutableListOf(primaryAddress),
      monitoringConditions = createMonitoringConditions(curfew = true),
      curfewTimetable = createCurfewTimeTable(
        startTime = startTime,
        endTime = endTime,
        curfewAddress = "PRIMARY_ADDRESS",
      ),
      curfewDayOfRelease = mockeDayOfRelease,
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.conditionalReleaseStartTime).isEqualTo(mockeDayOfRelease.startTime)
    assertThat(fmsMonitoringOrder.conditionalReleaseEndTime).isEqualTo(mockeDayOfRelease.endTime)
    assertThat(fmsMonitoringOrder.conditionalReleaseDate).isEqualTo(getBritishDate(mockeDayOfRelease.releaseDate))
  }

  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val londonTimeZone = ZoneId.of("Europe/London")

  private fun getBritishDate(dateTime: ZonedDateTime?): String? =
    dateTime?.toInstant()?.atZone(londonTimeZone)?.format(dateFormatter)

  @Test
  fun `should map Tag At Source as 'false' when installation location is primary address`() {
    val primaryAddress = createAddress(
      addressLine1 = "Primary Line 1",
      addressLine2 = "Primary Line 2",
      addressLine3 = "Primary Line 3",
      addressLine4 = "Primary Line 4",
      postcode = "Primary Post code",
      addressType = AddressType.PRIMARY,
    )

    val installationLocation = InstallationLocation(
      versionId = UUID.randomUUID(),
      location = InstallationLocationType.PRIMARY,
    )
    val order = createOrder(
      addresses = mutableListOf(primaryAddress),
      monitoringConditions = createMonitoringConditions(curfew = true, alcohol = false),
      installationLocation = installationLocation,
    )

    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, "")

    assertThat(fmsMonitoringOrder.tagAtSource).isEqualTo("false")

    assertThat(fmsMonitoringOrder.tagAtSourceDetails).isEqualTo("")
    assertThat(fmsMonitoringOrder.dateAndTimeInstallationWillTakePlace).isEqualTo("")

    assertThat(fmsMonitoringOrder.installationAddress1).isEqualTo("Primary Line 1")
    assertThat(fmsMonitoringOrder.installationAddressPostcode).isEqualTo("Primary Post code")
  }

  @Test
  fun `It should correctly map Tag At Source when installation location is a prison`() {
    val installationLocation = InstallationLocation(
      versionId = UUID.randomUUID(),
      location = InstallationLocationType.PRISON,
    )
    val installationAppointment = InstallationAppointment(
      versionId = UUID.randomUUID(),
      placeName = "HMP Wandsworth",
      appointmentDate = ZonedDateTime.of(2026, 10, 1, 10, 30, 0, 0, ZoneId.of("Europe/London")),
    )
    val trailMonitoringConditions = TrailMonitoringConditions(
      versionId = UUID.randomUUID(),
      startDate = ZonedDateTime.now(),
      endDate = ZonedDateTime.now().plusMonths(1),
    )

    val installationAddress = createAddress(
      addressType = AddressType.INSTALLATION,
      addressLine1 = "Installation Line 1",
      postcode = "Installation Post code",
    )

    val order = createOrder(
      addresses = mutableListOf(installationAddress),
      monitoringConditions = createMonitoringConditions(trail = true, alcohol = false),
      installationLocation = installationLocation,
      trailMonitoringConditions = trailMonitoringConditions,
    )

    order.installationAppointment = installationAppointment

    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.tagAtSource).isEqualTo("true")
    assertThat(fmsMonitoringOrder.tagAtSourceDetails).isEqualTo("HMP Wandsworth")
    assertThat(fmsMonitoringOrder.dateAndTimeInstallationWillTakePlace).isEqualTo("2026-10-01 10:30:00")

    assertThat(fmsMonitoringOrder.installationAddress1).isEqualTo("Installation Line 1")
    assertThat(fmsMonitoringOrder.installationAddressPostcode).isEqualTo("Installation Post code")
  }

  @Test
  fun `It should correctly map Tag At Source when installation location is a probation`() {
    val installationLocation = InstallationLocation(
      versionId = UUID.randomUUID(),
      location = InstallationLocationType.PROBATION_OFFICE,
    )
    val installationAppointment = InstallationAppointment(
      versionId = UUID.randomUUID(),
      placeName = "HMP Wandsworth",
      appointmentDate = ZonedDateTime.of(2026, 10, 1, 10, 30, 0, 0, ZoneId.of("Europe/London")),
    )
    val trailMonitoringConditions = TrailMonitoringConditions(
      versionId = UUID.randomUUID(),
      startDate = ZonedDateTime.now(),
      endDate = ZonedDateTime.now().plusMonths(1),
    )

    val installationAddress = createAddress(
      addressType = AddressType.INSTALLATION,
      addressLine1 = "Installation Line 1",
      postcode = "Installation Post code",
    )

    val order = createOrder(
      addresses = mutableListOf(installationAddress),
      monitoringConditions = createMonitoringConditions(trail = true, alcohol = false),
      installationLocation = installationLocation,
      trailMonitoringConditions = trailMonitoringConditions,
    )

    order.installationAppointment = installationAppointment

    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.tagAtSource).isEqualTo("true")
    assertThat(fmsMonitoringOrder.tagAtSourceDetails).isEqualTo("HMP Wandsworth")
    assertThat(fmsMonitoringOrder.dateAndTimeInstallationWillTakePlace).isEqualTo("2026-10-01 10:30:00")

    assertThat(fmsMonitoringOrder.installationAddress1).isEqualTo("Installation Line 1")
    assertThat(fmsMonitoringOrder.installationAddressPostcode).isEqualTo("Installation Post code")
  }

  @Test
  fun `It should map new details when changing between tag at source locations`() {
    val installationAddress = createAddress(
      addressType = AddressType.INSTALLATION,
      addressLine1 = "Installation Line 1",
      postcode = "Installation Post code",
    )

    val installationLocation = InstallationLocation(
      versionId = UUID.randomUUID(),
      location = InstallationLocationType.PRISON,
    )

    val installationAppointment = InstallationAppointment(
      versionId = UUID.randomUUID(),
      placeName = "HMP Wandsworth",
      appointmentDate = ZonedDateTime.of(2026, 10, 1, 10, 30, 0, 0, ZoneId.of("Europe/London")),
    )

    val trailMonitoringConditions = TrailMonitoringConditions(
      versionId = UUID.randomUUID(),
      startDate = ZonedDateTime.now(),
      endDate = ZonedDateTime.now().plusMonths(1),
    )

    val order = createOrder(
      addresses = mutableListOf(
        installationAddress,
      ),
      monitoringConditions = createMonitoringConditions(trail = true),
      installationLocation = installationLocation,
      trailMonitoringConditions = trailMonitoringConditions,
    )
    order.installationAppointment = installationAppointment

    order.installationLocation!!.location = InstallationLocationType.PROBATION_OFFICE
    order.installationAppointment = null
    order.addresses.removeAll { it.addressType == AddressType.INSTALLATION }

    order.addresses.add(
      createAddress(
        addressType = AddressType.INSTALLATION,
        addressLine1 = "New Probation Address Line 1",
        addressLine2 = "New Probation Address Line 2",
        postcode = "New Probation Postcode",
      ),
    )
    order.installationAppointment = InstallationAppointment(
      versionId = UUID.randomUUID(),
      placeName = "London",
      appointmentDate = ZonedDateTime.of(2026, 11, 15, 14, 30, 0, 0, ZoneId.of("Europe/London")),
    )

    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.tagAtSource).isEqualTo("true")
    assertThat(fmsMonitoringOrder.tagAtSourceDetails).isEqualTo("London")
    assertThat(fmsMonitoringOrder.dateAndTimeInstallationWillTakePlace).isEqualTo("2026-11-15 14:30:00")
    assertThat(fmsMonitoringOrder.installationAddress1).isEqualTo("New Probation Address Line 1")
    assertThat(fmsMonitoringOrder.installationAddress2).isEqualTo("New Probation Address Line 2")
    assertThat(fmsMonitoringOrder.installationAddressPostcode).isEqualTo("New Probation Postcode")
  }

  @ParameterizedTest(name = "it should map probation delivery unit to Serco - {0} -> {1}")
  @ArgumentsSource(ProbationDeliveryUnitArgumentsProvider::class)
  fun `It should correctly map saved probation delivery unit values to Serco`(
    savedValue: String,
    mappedValue: String,
  ) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(),
      installationAndRisk = createInstallationAndRisk(riskCategory = savedValue),
      interestedParties = createInterestedParty(responsibleOrganisation = "PROBATION"),
      probationDeliveryUnits = createProbationDeliveryUnit(savedValue),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.pduResponsible).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map DDv6 probation delivery unit to Serco - {0} -> {1}")
  @ArgumentsSource(ProbationDeliveryUnitDDv6ArgumentsProvider::class)
  fun `It should correctly map DDv6 saved probation delivery unit values`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
      deviceWearer = createDeviceWearer(),
      installationAndRisk = createInstallationAndRisk(riskCategory = savedValue),
      interestedParties = createInterestedParty(responsibleOrganisation = "PROBATION"),
      probationDeliveryUnits = createProbationDeliveryUnit(savedValue),
    )

    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.pduResponsible).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map notifying organisation - {0} -> {1}")
  @ArgumentsSource(NotifyingOrganisationArgumentsProvider::class)
  fun `It should correctly map notifying organisation to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV5,
      deviceWearer = createDeviceWearer(),
      interestedParties = createInterestedParty(
        responsibleOrganisation = "PROBATION",
        notifyingOrganisation = savedValue,
      ),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.notifyingOrganization).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map civil county court - {0} -> {1}")
  @ArgumentsSource(CivilAndCountyCourtArgumentsProvider::class)
  fun `It should correctly map saved civil county court values to Serco`(savedValue: String, mappedValue: String) {
    assertNotifyingOrgNameMapping(savedValue, mappedValue)
  }

  @ParameterizedTest(name = "it should map crown court - {0} -> {1}")
  @ArgumentsSource(CrownCourtArgumentsProvider::class)
  fun `It should correctly map saved crown court values to Serco`(savedValue: String, mappedValue: String) {
    assertNotifyingOrgNameMapping(savedValue, mappedValue)
  }

  @ParameterizedTest(name = "it should map family court - {0} -> {1}")
  @ArgumentsSource(FamilyCourtArgumentsProvider::class)
  fun `It should correctly map saved family court values to Serco`(savedValue: String, mappedValue: String) {
    assertNotifyingOrgNameMapping(savedValue, mappedValue)
  }

  @ParameterizedTest(name = "it should map magistrates court - {0} -> {1}")
  @ArgumentsSource(MagistratesCourtArgumentsProvider::class)
  fun `It should correctly map saved magistrates court values to Serco`(savedValue: String, mappedValue: String) {
    assertNotifyingOrgNameMapping(savedValue, mappedValue)
  }

  @ParameterizedTest(name = "it should map military court - {0} -> {1}")
  @ArgumentsSource(MilitaryCourtArgumentsProvider::class)
  fun `It should correctly map saved military court values to Serco`(savedValue: String, mappedValue: String) {
    assertNotifyingOrgNameMapping(savedValue, mappedValue)
  }

  @ParameterizedTest(name = "it should map prison - {0} -> {1}")
  @ArgumentsSource(PrisonArgumentsProvider::class)
  fun `It should correctly map saved prison values to Serco`(savedValue: String, mappedValue: String) {
    assertNotifyingOrgNameMapping(savedValue, mappedValue)
  }

  @ParameterizedTest(name = "it should map youth court - {0} -> {1}")
  @ArgumentsSource(YouthCourtArgumentsProvider::class)
  fun `It should correctly map saved youth court values to Serco`(savedValue: String, mappedValue: String) {
    assertNotifyingOrgNameMapping(savedValue, mappedValue)
  }

  @ParameterizedTest(name = "it should map pilot to Serco - {0} -> {1}")
  @ArgumentsSource(PilotArgumentsProvider::class)
  fun `It should correctly map saved pilots values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      monitoringConditions = createMonitoringConditions(pilot = Pilot.entries.first { it.name == savedValue }),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.pilot).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map alcohol condition types - {0} -> {1}")
  @ArgumentsSource(AlcoholNotifyingOrganisationArgumentsProvider::class)
  fun `It should correctly map alcohol condition types to Serco`(
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
  @ArgumentsSource(AlcoholAbstinenceArgumentsProvider::class)
  fun `It should correctly map alcohol abstinence to Serco`(savedValue: AlcoholMonitoringType, mappedValue: String) {
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

  @ParameterizedTest(name = "it should map sentence type - {0} -> {1}")
  @ArgumentsSource(SentenceArgumentsProvider::class)
  fun `It should correctly map sentence type to Serco`(savedValue: String, mappedValue: String) {
    val sentenceType = SentenceType.entries.first { it.name == savedValue }
    val order = createOrder(
      monitoringConditions = createMonitoringConditions(sentenceType = sentenceType),
    )

    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, "")
    assertThat(fmsMonitoringOrder.sentenceType).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map order type to subcategory - {0} -> {1}")
  @ArgumentsSource(OrderTypeArgumentsProvider::class)
  fun `It should correctly map subcategory to Serco`(savedValue: RequestType, mappedValue: String) {
    val order = createOrder(
      type = savedValue,
      variationDetails = createvariationDetails(),
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, "")
    assertThat(fmsMonitoringOrder.subcategory).isEqualTo(mappedValue)
  }

  @Test
  fun `It should correctly map subcategory when order type is bail`() {
    val order = createOrder(
      type = RequestType.REVOCATION,
      monitoringConditions = createMonitoringConditions(orderType = OrderType.BAIL),
      variationDetails = createvariationDetails(),
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, "")
    assertThat(fmsMonitoringOrder.subcategory).isEqualTo("SR11 - Removal of devices (bail)")
  }

  @Test
  fun `It should correctly map subcategory when order type is immigration`() {
    val order = createOrder(
      type = RequestType.REVOCATION,
      monitoringConditions = createMonitoringConditions(orderType = OrderType.IMMIGRATION),
      variationDetails = createvariationDetails(),
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, "")
    assertThat(fmsMonitoringOrder.subcategory).isEqualTo("SR11 - Removal of devices (bail)")
  }

  @Test
  fun `It should map empty PROBATION name to 'Probation Board' for Serco`() {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV5,
      interestedParties = createInterestedParty(
        notifyingOrganisation = NotifyingOrganisationDDv5.PROBATION.name,
        notifyingOrganisationName = "",
      ),
    )

    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)
    assertThat(fmsMonitoringOrder.noName).isEqualTo("Probation Board")
  }

  @Test
  fun `It should map empty YCS name to empty string for Serco`() {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV5,
      interestedParties = createInterestedParty(
        notifyingOrganisation = NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE.name,
        notifyingOrganisationName = "",
      ),
    )

    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.noName).isEqualTo("")
  }

  private fun assertNotifyingOrgNameMapping(savedValue: String, mappedValue: String) {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV5,
      deviceWearer = createDeviceWearer(),
      interestedParties = createInterestedParty(
        responsibleOrganisation = "PROBATION",
        notifyingOrganisationName = savedValue,
      ),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.noName).isEqualTo(mappedValue)
  }
}
