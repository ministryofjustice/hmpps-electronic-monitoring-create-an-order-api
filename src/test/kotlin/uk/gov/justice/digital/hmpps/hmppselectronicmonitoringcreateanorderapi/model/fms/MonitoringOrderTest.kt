package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.OrderTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Zone
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.offenceAdditionalDetails).isEqualTo("Mock Additional Details")
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

  @ParameterizedTest(name = "it should map probation delivery unit to Serco - {0} -> {1}")
  @MethodSource("getProbationDeliveryUnitValues")
  fun `It should map correctly map saved probation delivery unit values to Serco`(
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

  @ParameterizedTest(name = "it should map civilCountyCourt - {0} -> {1}")
  @MethodSource("getCivilCountyCourtValues")
  fun `It should map correctly map saved civilCountyCourt values to Serco`(savedValue: String, mappedValue: String) {
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

  @ParameterizedTest(name = "it should map prison - {0} -> {1}")
  @MethodSource("getPrisonValues")
  fun `It should map correctly map saved prison values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(),
      interestedParties = createInterestedParty(
        responsibleOrganisation = "PROBATION",
        notifyingOrganisationName = savedValue,
      ),

    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.noName).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map pilot to Serco - {0} -> {1}")
  @MethodSource("getPilots")
  fun `It should map correctly map saved pilots values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      monitoringConditions = createMonitoringConditions(pilot = Pilot.entries.first { it.name == savedValue }),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order, null)

    assertThat(fmsMonitoringOrder.pilot).isEqualTo(mappedValue)
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
    fun getCivilCountyCourtValues() = listOf(
      Arguments.of("ABERYSTWYTH_COUNTY_AND_CIVIL_COURT", "Aberystwyth County and Civil Court"),
      Arguments.of("ALDERSHOT_COUNTY_AND_CIVIL_COURT", "Aldershot County and Civil Court"),
      Arguments.of("BARNET_COUNTY_AND_CIVIL_COURT", "Barnet County and Civil Court"),
      Arguments.of("BARNSLEY_COUNTY_AND_CIVIL_COURT", "Barnsley County and Civil Court"),
      Arguments.of("BARNSTABLE_COUNTY_AND_CIVIL_COURT", "Barnstable County and Civil Court"),
      Arguments.of("BARROW_COUNTY_AND_CIVIL_COURT", "Barrow County and Civil Court"),
      Arguments.of("BASILDON_COUNTY_AND_CIVIL_COURT", "Basildon County and Civil Court"),
      Arguments.of("BASINGSTOKE_COUNTY_AND_CIVIL_COURT", "Basingstoke County and Civil Court"),
      Arguments.of("BATH_COUNTY_AND_CIVIL_COURT", "Bath County and Civil Court"),
      Arguments.of("BEDFORD_COUNTY_AND_CIVIL_COURT", "Bedford County and Civil Court"),
      Arguments.of("BIRKENHEAD_COUNTY_AND_CIVIL_COURT", "Birkenhead County and Civil Court"),
      Arguments.of("BIRMINGHAM_COUNTY_AND_CIVIL_COURT", "Birmingham County and Civil Court"),
      Arguments.of("BLACKBURN_COUNTY_AND_CIVIL_COURT", "Blackburn County and Civil Court"),
      Arguments.of("BLACKPOOL_COUNTY_AND_CIVIL_COURT", "Blackpool County and Civil Court"),
      Arguments.of("BLACKWOOD_COUNTY_AND_CIVIL_COURT", "Blackwood County and Civil Court"),
      Arguments.of("BODMIN_COUNTY_AND_CIVIL_COURT", "Bodmin County and Civil Court"),
      Arguments.of("BOSTON_COUNTY_AND_CIVIL_COURT", "Boston County and Civil Court"),
      Arguments.of("BOURNEMOUTH_COUNTY_AND_CIVIL_COURT", "Bournemouth County and Civil Court"),
      Arguments.of("BRADFORD_COUNTY_AND_CIVIL_COURT", "Bradford County and Civil Court"),
      Arguments.of("BRENTFORD_COUNTY_AND_CIVIL_COURT", "Brentford County and Civil Court"),
      Arguments.of("BRIGHTON_COUNTY_AND_CIVIL_COURT", "Brighton County and Civil Court"),
      Arguments.of("BRISTOL_COUNTY_AND_CIVIL_COURT", "Bristol County and Civil Court"),
      Arguments.of("BROMLEY_COUNTY_AND_CIVIL_COURT", "Bromley County and Civil Court"),
      Arguments.of("BURNLEY_COUNTY_AND_CIVIL_COURT", "Burnley County and Civil Court"),
      Arguments.of("BURY_ST_EDMONDS_COUNTY_AND_CIVIL_COURT", "Bury St Edmonds County and Civil Court"),
      Arguments.of("CAERNARFON_COUNTY_AND_CIVIL_COURT", "Caernarfon County and Civil Court"),
      Arguments.of("CAMBRIDGE_COUNTY_AND_CIVIL_COURT", "Cambridge County and Civil Court"),
      Arguments.of("CANTERBURY_COUNTY_AND_CIVIL_COURT", "Canterbury County and Civil Court"),
      Arguments.of("CARDIFF_COUNTY_AND_CIVIL_COURT", "Cardiff County and Civil Court"),
      Arguments.of("CARLISLE_COUNTY_AND_CIVIL_COURT", "Carlisle County and Civil Court"),
      Arguments.of("CARMARTHEN_COUNTY_AND_CIVIL_COURT", "Carmarthen (hearings only) County and Civil Court"),
      Arguments.of("CENTRAL_LONDON_COUNTY_AND_CIVIL_COURT", "Central London County and Civil Court"),
      Arguments.of("CHELMSFORD_COUNTY_AND_CIVIL_COURT", "Chelmsford County and Civil Court"),
      Arguments.of("CHESTER_COUNTY_AND_CIVIL_COURT", "Chester County and Civil Court"),
      Arguments.of("CHESTERFIELD_COUNTY_AND_CIVIL_COURT", "Chesterfield County and Civil Court"),
      Arguments.of(
        "CLERKENWELL_AND_SHOREDITCH_COUNTY_AND_CIVIL_COURT",
        "Clerkenwell & Shoreditch County and Civil Court",
      ),
      Arguments.of(
        "COURT_OF_PROTECTION_COURT_COUNTY_AND_CIVIL_COURT",
        "Court of Protection Court County and Civil Court",
      ),
      Arguments.of("COVENTRY_COUNTY_AND_CIVIL_COURT", "Coventry County and Civil Court"),
      Arguments.of("CREWE_COUNTY_AND_CIVIL_COURT", "Crewe County and Civil Court"),
      Arguments.of("CROYDON_COUNTY_AND_CIVIL_COURT", "Croydon County and Civil Court"),
      Arguments.of("DARLINGTON_COUNTY_AND_CIVIL_COURT", "Darlington County and Civil Court"),
      Arguments.of("DARTFORD_COUNTY_AND_CIVIL_COURT", "Dartford County and Civil Court"),
      Arguments.of("DERBY_COUNTY_AND_CIVIL_COURT", "Derby County and Civil Court"),
      Arguments.of("DONCASTER_COUNTY_AND_CIVIL_COURT", "Doncaster County and Civil Court"),
      Arguments.of("DUDLEY_COUNTY_AND_CIVIL_COURT", "Dudley County and Civil Court"),
      Arguments.of("DURHAM_COUNTY_AND_CIVIL_COURT", "Durham County and Civil Court"),
      Arguments.of("EDMONTON_COUNTY_AND_CIVIL_COURT", "Edmonton County and Civil Court"),
      Arguments.of("EXETER_COUNTY_AND_CIVIL_COURT", "Exeter County and Civil Court"),
      Arguments.of("GATESHEAD_COUNTY_AND_CIVIL_COURT", "Gateshead County and Civil Court"),
      Arguments.of("GLOUCESTER_COUNTY_AND_CIVIL_COURT", "Gloucester County and Civil Court"),
      Arguments.of("GRIMSBY_COUNTY_AND_CIVIL_COURT", "Grimsby County and Civil Court"),
      Arguments.of("GUILDFORD_COUNTY_AND_CIVIL_COURT", "Guildford County and Civil Court"),
      Arguments.of("HARROGATE_COUNTY_AND_CIVIL_COURT", "Harrogate County and Civil Court"),
      Arguments.of("HASTINGS_COUNTY_AND_CIVIL_COURT", "Hastings County and Civil Court"),
      Arguments.of("HAVERFORDWEST_COUNTY_AND_CIVIL_COURT", "Haverfordwest County and Civil Court"),
      Arguments.of("HEREFORD_COUNTY_AND_CIVIL_COURT", "Hereford County and Civil Court"),
      Arguments.of("HERTFORD_COUNTY_AND_CIVIL_COURT", "Hertford County and Civil Court"),
      Arguments.of("HIGH_WYCOMBE_COUNTY_AND_CIVIL_COURT", "High Wycombe County and Civil Court"),
      Arguments.of("HORSHAM_COUNTY_AND_CIVIL_COURT", "Horsham County and Civil Court"),
      Arguments.of("HUDDERSFIELD_COUNTY_AND_CIVIL_COURT", "Huddersfield County and Civil Court"),
      Arguments.of("IPSWICH_COUNTY_AND_CIVIL_COURT", "Ipswich County and Civil Court"),
      Arguments.of("KINGSTON_UPON_HULL_COUNTY_AND_CIVIL_COURT", "Kingston Upon Hull County and Civil Court"),
      Arguments.of("KINGSTON_UPON_THAMES_COUNTY_AND_CIVIL_COURT", "Kingston upon Thames County and Civil Court"),
      Arguments.of("LANCASTER_COUNTY_AND_CIVIL_COURT", "Lancaster County and Civil Court"),
      Arguments.of("LEEDS_COUNTY_AND_CIVIL_COURT", "Leeds County and Civil Court"),
      Arguments.of("LEICESTER_COUNTY_AND_CIVIL_COURT", "Leicester County and Civil Court"),
      Arguments.of("LEWES_COUNTY_AND_CIVIL_COURT", "Lewes County and Civil Court"),
      Arguments.of("LINCOLN_COUNTY_AND_CIVIL_COURT", "Lincoln County and Civil Court"),
      Arguments.of("LIVERPOOL_COUNTY_AND_CIVIL_COURT", "Liverpool County and Civil Court"),
      Arguments.of(
        "LLANDRINDOD_WELLS__COUNTY_AND_CIVIL_COURT",
        "Llandrindod Wells (hearings only) County and Civil Court",
      ),
      Arguments.of(
        "LLANELLI_COUNTY_AND_CIVIL_COURT",
        "Llanelli County and Civil Court",
      ),
      Arguments.of("LUTON_COUNTY_AND_CIVIL_COURT", "Luton County and Civil Court"),
      Arguments.of("MAIDSTONE_COUNTY_AND_CIVIL_COURT", "Maidstone County and Civil Court"),
      Arguments.of("MANCHESTER_COUNTY_AND_CIVIL_COURT", "Manchester County and Civil Court"),
      Arguments.of("MANSFIELD__COUNTY_AND_CIVIL_COURT", "Mansfield  County and Civil Court"),
      Arguments.of(
        "MAYORS_AND_CITY_OF_LONDON_COUNTY_AND_CIVIL_COURT",
        "Mayors & City of London County and Civil Court",
      ),
      Arguments.of("MEDWAY_COUNTY_AND_CIVIL_COURT", "Medway County and Civil Court"),
      Arguments.of("MERTHYR_COUNTY_AND_CIVIL_COURT", "Merthyr County and Civil Court"),
      Arguments.of("MILTON_KEYNES_COUNTY_AND_CIVIL_COURT", "Milton Keynes County and Civil Court"),
      Arguments.of("MOLD_COUNTY_AND_CIVIL_COURT", "Mold (hearings only)  County and Civil Court"),
      Arguments.of("NEWCASTLE_COUNTY_AND_CIVIL_COURT", "Newcastle County and Civil Court"),
      Arguments.of("NEWPORT_IOW_COUNTY_AND_CIVIL_COURT", "Newport (IOW) County and Civil Court"),
      Arguments.of("NEWPORT_SOUTH_WALES_COUNTY_AND_CIVIL_COURT", "Newport (South Wales)  County and Civil Court"),
      Arguments.of("NORTH_SHIELDS_COUNTY_AND_CIVIL_COURT", "North Shields County and Civil Court"),
      Arguments.of("NORTHAMPTON_COUNTY_AND_CIVIL_COURT", "Northampton County and Civil Court"),
      Arguments.of("NORWICH_COUNTY_AND_CIVIL_COURT", "Norwich County and Civil Court"),
      Arguments.of("NOTTINGHAM_COUNTY_AND_CIVIL_COURT", "Nottingham County and Civil Court"),
      Arguments.of("NUNEATON_COUNTY_AND_CIVIL_COURT", "Nuneaton County and Civil Court"),
      Arguments.of("OXFORD_COUNTY_AND_CIVIL_COURT", "Oxford County and Civil Court"),
      Arguments.of("PETERBOROUGH_COUNTY_AND_CIVIL_COURT", "Peterborough County and Civil Court"),
      Arguments.of("PLYMOUTH_COUNTY_AND_CIVIL_COURT", "Plymouth County and Civil Court"),
      Arguments.of("PONTYPRIDD_COUNTY_AND_CIVIL_COURT", "Pontypridd County and Civil Court"),
      Arguments.of("PORT_TALBOT_COUNTY_AND_CIVIL_COURT", "Port Talbot County and Civil Court"),
      Arguments.of("PORTSMOUTH_COUNTY_AND_CIVIL_COURT", "Portsmouth County and Civil Court"),
      Arguments.of("PRESTATYN_COUNTY_AND_CIVIL_COURT", "Prestatyn County and Civil Court"),
      Arguments.of("PRESTON_COUNTY_AND_CIVIL_COURT", "Preston County and Civil Court"),
      Arguments.of("READING_COUNTY_AND_CIVIL_COURT", "Reading County and Civil Court"),
      Arguments.of("ROMFORD_COUNTY_AND_CIVIL_COURT", "Romford County and Civil Court"),
      Arguments.of("SALISBURY_COUNTY_AND_CIVIL_COURT", "Salisbury County and Civil Court"),
      Arguments.of("SCARBOROUGH_COUNTY_AND_CIVIL_COURT", "Scarborough County and Civil Court"),
      Arguments.of("SHEFFIELD_COUNTY_AND_CIVIL_COURT", "Sheffield County and Civil Court"),
      Arguments.of("SKIPTON_COUNTY_AND_CIVIL_COURT", "Skipton County and Civil Court"),
      Arguments.of("SLOUGH_COUNTY_AND_CIVIL_COURT", "Slough County and Civil Court"),
      Arguments.of("SOUTH_SHIELDS_COUNTY_AND_CIVIL_COURT", "South Shields County and Civil Court"),
      Arguments.of("SOUTHAMPTON_COUNTY_AND_CIVIL_COURT", "Southampton County and Civil Court"),
      Arguments.of("SOUTHEND_COUNTY_AND_CIVIL_COURT", "Southend County and Civil Court"),
      Arguments.of("ST_HELENS_COUNTY_AND_CIVIL_COURT", "St Helens County and Civil Court"),
      Arguments.of(
        "STAINES_COUNTY_AND_CIVIL_COURT",
        "Staines County and Civil Court",
      ),
      Arguments.of("STOCKPORT_COUNTY_AND_CIVIL_COURT", "Stockport County and Civil Court"),
      Arguments.of("STOKE_ON_TRENT_COUNTY_AND_CIVIL_COURT", "Stoke on Trent County and Civil Court"),
      Arguments.of("SUNDERLAND_COUNTY_AND_CIVIL_COURT", "Sunderland County and Civil Court"),
      Arguments.of("SWANSEA_COUNTY_AND_CIVIL_COURT", "Swansea County and Civil Court"),
      Arguments.of("SWINDON_COUNTY_AND_CIVIL_COURT", "Swindon County and Civil Court"),
      Arguments.of("TAUNTON_COUNTY_AND_CIVIL_COURT", "Taunton County and Civil Court"),
      Arguments.of("TEESIDE_COUNTY_AND_CIVIL_COURT", "Teeside County and Civil Court"),
      Arguments.of("TELFORD_COUNTY_AND_CIVIL_COURT", "Telford County and Civil Court"),
      Arguments.of("THANET_COUNTY_AND_CIVIL_COURT", "Thanet County and Civil Court"),
      Arguments.of(
        "TORQUAY_AND_NEWTON_ABBOTT_COUNTY_AND_CIVIL_COURT",
        "Torquay & Newton Abbott County and Civil Court",
      ),
      Arguments.of("TRURO_COUNTY_AND_CIVIL_COURT", "Truro County and Civil Court"),
      Arguments.of("UXBRIDGE_COUNTY_AND_CIVIL_COURT", "Uxbridge County and Civil Court"),
      Arguments.of("WAKEFIELD_COUNTY_AND_CIVIL_COURT", "Wakefield County and Civil Court"),
      Arguments.of("WALSALL_COUNTY_AND_CIVIL_COURT", "Walsall County and Civil Court"),
      Arguments.of("WANDSWORTH_COUNTY_AND_CIVIL_COURT", "Wandsworth County and Civil Court"),
      Arguments.of("WARWICK_COUNTY_AND_CIVIL_COURT", "Warwick County and Civil Court"),
      Arguments.of("WATFORD_COUNTY_AND_CIVIL_COURT", "Watford County and Civil Court"),
      Arguments.of("WELSHPOOL_COUNTY_AND_CIVIL_COURT", "Welshpool (hearings only) County and Civil Court"),
      Arguments.of("WEST_CUMBRIA_COUNTY_AND_CIVIL_COURT", "West Cumbria (aka Workington) County and Civil Court"),
      Arguments.of("WESTON_SUPER_MARE_COUNTY_AND_CIVIL_COURT", "Weston Super Mare County and Civil Court"),
      Arguments.of("WEYMOUTH_COUNTY_AND_CIVIL_COURT", "Weymouth County and Civil Court"),
      Arguments.of("WIGAN_COUNTY_AND_CIVIL_COURT", "Wigan County and Civil Court"),
      Arguments.of("WILLESDEN_COUNTY_AND_CIVIL_COURT", "Willesden County and Civil Court"),
      Arguments.of("WINCHESTER_COUNTY_AND_CIVIL_COURT", "Winchester County and Civil Court"),
      Arguments.of("WOLVERHAMPTON_COUNTY_AND_CIVIL_COURT", "Wolverhampton County and Civil Court"),
      Arguments.of("WORCESTER_COUNTY_AND_CIVIL_COURT", "Worcester County and Civil Court"),
      Arguments.of("WORTHING_COUNTY_AND_CIVIL_COURT", "Worthing County and Civil Court"),
      Arguments.of("WREXHAM_COUNTY_AND_CIVIL_COURT", "Wrexham County and Civil Court"),
      Arguments.of("YEOVIL_COUNTY_AND_CIVIL_COURT", "Yeovil County and Civil Court"),
      Arguments.of("YORK_COUNTY_AND_CIVIL_COURT", "York County and Civil Court"),
    )

    @JvmStatic
    fun getProbationDeliveryUnitValues() = listOf(
      Arguments.of("BARKING_AND_DAGENHAM_AND_HAVERING", "Barking and Dagenham and Havering"),
      Arguments.of("BARNSLEY_AND_ROTHERHAM", "Barnsley and Rotherham"),
      Arguments.of("BATH_AND_NORTH_SOMERSET", "Bath and North Somerset"),
      Arguments.of("BEDFORDSHIRE", "Bedfordshire"),
      Arguments.of("BIRMINGHAM_CENTRAL_AND_SOUTH", "Birmingham Central and South"),
      Arguments.of("BIRMINGHAM_COURTS_AND_DENTRALISED_FUNCTIONS", "Birmingham Courts and Centralised Functions"),
      Arguments.of("BIRMINGHAM_NORTH_EAST_AND_SOLIHULL", "Birmingham North, East and Solihull"),
      Arguments.of("BLACKBURN", "Blackburn"),
      Arguments.of("BOLTON", "Bolton"),
      Arguments.of("BRADFORD_AND_CALDERDALE", "Bradford and Calderdale"),
      Arguments.of("BRENT", "Brent"),
      Arguments.of("BRISTOL_AND_SOUTH_GLOUCESTERSHIRE", "Bristol and South Gloucestershire"),
      Arguments.of("BUCKINGHAM_AND_MILTON_KEYNES", "Buckinghamshire and Milton Keynes"),
      Arguments.of("BURY_AND_ROCHDALE", "Bury and Rochdale"),
      Arguments.of("CAMBRIDGESHIRE", "Cambridgeshire"),
      Arguments.of("CAMDEN_AND_ISLINGTON", "Camden and Islington"),
      Arguments.of("CARDIFF_AND_THE_VALE", "Cardiff and the Vale"),
      Arguments.of("CENTRAL_LANCASHIRE", "Central Lancashire"),
      Arguments.of("CHESHIRE_EAST", "Cheshire East"),
      Arguments.of("CHESHIRE_WEST", "Cheshire West"),
      Arguments.of("CORNWALL_AND_ISLES_OF_SCILLY", "Cornwall and Isles of Scilly"),
      Arguments.of("COUNTY_DURHAM_AND_DARLINGTON", "County Durham and Darlington"),
      Arguments.of("COVENTRY", "Coventry"),
      Arguments.of("CROYDON", "Croydon"),
      Arguments.of("CUMBRIA", "Cumbria"),
      Arguments.of("CWM_TAF_MORGANNWG", "Cwm Taf Morgannwg"),
      Arguments.of("DERBY_CITY", "Derby City"),
      Arguments.of("DERBYSHIRE", "Derbyshire"),
      Arguments.of("DEVON_AND_TORBAY", "Devon and Torbay"),
      Arguments.of("DONCASTER", "Doncaster"),
      Arguments.of("DORSET", "Dorset"),
      Arguments.of("DUDLEY_AND_SANDWELL", "Dudley and Sandwell"),
      Arguments.of("DYFED_POWYS", "Dyfed Powys"),
      Arguments.of("EALING_AND_HILLINGDOM", "Ealing and Hillingdom"),
      Arguments.of("EAST_AND_WEST_LINCOLNSHIRE", "East and West Lincolnshire"),
      Arguments.of("EAST_BERKSHIRE", "East Berkshire"),
      Arguments.of("EAST_KENT", "East Kent"),
      Arguments.of("EAST_LANCASHIRE", "East Lancashire"),
      Arguments.of("EAST_SUSSEX", "East Sussex"),
      Arguments.of("ENFIELD_AND_HARINGEY", "Enfield and Haringey"),
      Arguments.of("ESSEX_NORTH", "Essex North"),
      Arguments.of("ESSEX_SOUTH", "Essex South"),
      Arguments.of("GATESHEAD_AND_SOUTH_TYNESIDE", "Gateshead and South Tyneside"),
      Arguments.of("GLOUCESTERSHIRE", "Gloucestershire"),
      Arguments.of("GREENWICH_AND_BEXLEY", "Greenwich and Bexley"),
      Arguments.of("GWENT", "Gwent"),
      Arguments.of("HACKNEY_AND_CITY", "Hackney and City"),
      Arguments.of(
        "HAMMERSMITH_FULHAM_KENSINGTON_CHELSEA_AND_WESTMINSTER",
        "Hammersmith, Fulham, Kensington, Chelsea and Westminster",
      ),
      Arguments.of("HAMPSHIRE_NORTH_AND_EAST", "Hampshire North and East"),
      Arguments.of("HAMPSHIRE_SOUTH_AND_ISLE_OF_WHITE", "Hampshire South and Isle of White"),
      Arguments.of("HAMPSHIRE_SOUTH_WEST", "Hampshire South West"),
      Arguments.of("HARROW_AND_BARNET", "Harrow and Barnet"),
      Arguments.of("HEREFORD_SHROPSHIRE_AND_TELFORD", "Hereford, Shropshire and Telford"),
      Arguments.of("HERTFORDSHIRE", "Hertfordshire"),
      Arguments.of("HULL_AND_EAST_RIDING", "Hull and East Riding"),
      Arguments.of("KINGSTON_RICHMOND_AND_HOUNSLOW", "Kingston, Richmond and Hounslow"),
      Arguments.of("KIRKLEES", "Kirklees"),
      Arguments.of("KNOWSLEY_AND_ST_HELENS", "Knowsley and St Helens"),
      Arguments.of("LAMBETH", "Lambeth"),
      Arguments.of("LEEDS", "Leeds"),
      Arguments.of("LEICESTER_LEICESTERSHIRE_AND_RUTLAND", "Leicester, Leicestershire and Rutland"),
      Arguments.of("LEWISHAM_AND_BROMLEY", "Lewisham and Bromley"),
      Arguments.of("LIVERPOOL_NORTH", "Liverpool North"),
      Arguments.of("LIVERPOOL_SOUTH", "Liverpool South"),
      Arguments.of("MANCHESTER_NORTH", "Manchester North"),
      Arguments.of("MANCHESTER_SOUTH", "Manchester South"),
      Arguments.of("NEWCASTLE_UPON_TYNE", "Newcastle Upon Tyne"),
      Arguments.of("NEWHAM", "Newham"),
      Arguments.of("NORFOLK", "Norfolk"),
      Arguments.of("NORTH_AND_NORTH_EAST_LINCS", "North and North East Lincs"),
      Arguments.of("NORTH_KENT_AND_MEDWAY", "North Kent and Medway"),
      Arguments.of("NORTH_TYNESIDE_AND_NORTHUMBERLAND", "North Tyneside and Northumberland"),
      Arguments.of("NORTH_WALES", "North Wales"),
      Arguments.of("NORTH_WEST_LANCASHIRE", "North West Lancashire"),
      Arguments.of("NORTH_YORKSHIRE", "North Yorkshire"),
      Arguments.of("NORTHAMPTONSHIRE", "Northamptonshire"),
      Arguments.of("NOTTINGHAM_CITY", "Nottingham City"),
      Arguments.of("NOTTINGHAMSHIRE", "Nottinghamshire"),
      Arguments.of("OLDHAM", "Oldham"),
      Arguments.of("OXFORDSHIRE", "Oxfordshire"),
      Arguments.of("PLYMOUTH", "Plymouth"),
      Arguments.of("REDBRIDGE_AND_WALTHAM_FOREST", "Redbridge and Waltham Forest"),
      Arguments.of("REDCAR_CLEVELAND_AND_MIDDLESBROUGH", "Redcar, Cleveland and Middlesbrough"),
      Arguments.of("SALFORD", "Salford"),
      Arguments.of("SEFTON_AND_MERSEYSIDE_WOMENS", "Sefton and Merseyside Womens"),
      Arguments.of("SHEFFIELD", "Sheffield"),
      Arguments.of("SOMERSET", "Somerset"),
      Arguments.of("SOUTHWARK", "Southwark"),
      Arguments.of("STAFFORDSHIRE_AND_STOKE", "Staffordshire and Stoke"),
      Arguments.of("STOCKPORT_AND_TRAFFORD", "Stockport and Trafford"),
      Arguments.of("STOCKTON_AND_HARTLEPOOL", "Stockton and Hartlepool"),
      Arguments.of("SUFFOLK", "Suffolk"),
      Arguments.of("SUNDERLAND", "Sunderland"),
      Arguments.of("SURREY", "Surrey"),
      Arguments.of("SWANSEA_NEATH_PORT_TALBOT", "Swansea, Neath and Port-Talbot"),
      Arguments.of("SWINDON_AND_WILTSHIRE", "Swindon and Wiltshire"),
      Arguments.of("TAMESIDE", "Tameside"),
      Arguments.of("TOWER_HAMLETS", "Tower Hamlets"),
      Arguments.of("WAKEFIELD", "Wakefield"),
      Arguments.of("WALSALL_AND_WOLVERHAMPTON", "Walsall and Wolverhampton"),
      Arguments.of("WANDSWORTH_MERTON_AND_SUTTON", "Wandsworth, Merton and Sutton"),
      Arguments.of("WARRINGTON_AND_HALTON", "Warrington and Halton"),
      Arguments.of("WARWICKSHIRE", "Warwickshire"),
      Arguments.of("WEST_BERKSHIRE", "West Berkshire"),
      Arguments.of("WEST_KENT", "West Kent"),
      Arguments.of("WEST_SUSSEX", "West Sussex"),
      Arguments.of("WIGAN", "Wigan"),
      Arguments.of("WIRRAL_AND_ISC_TEAM", "Wirral and ISC Team"),
      Arguments.of("WORCESTERSHIRE", "Worcestershire"),
      Arguments.of("YORK", "York"),
    )

    @JvmStatic
    fun getPrisonValues() = listOf(
      Arguments.of("ALTCOURSE_PRISON", "Altcourse Prison"),
      Arguments.of("ASHFIELD_PRISON", "Ashfield Prison"),
      Arguments.of(
        "ASKHAM_GRANGE_PRISON_AND_YOUNG_OFFENDER_INSTITUTION",
        "Askham Grange Prison and Young Offender Institution",
      ),
      Arguments.of(
        "AYLESBURY_PRISON_AND_YOUNG_OFFENDER_INSTITUTION",
        "Aylesbury Prison and Young Offender Institution",
      ),
      Arguments.of("BEDFORD_PRISON", "Bedford Prison"),
      Arguments.of("BELMARSH_PRISON", "Belmarsh Prison"),
      Arguments.of("BERWYN_PRISON", "Berwyn Prison"),
      Arguments.of("BIRMINGHAM_PRISON", "Birmingham Prison"),
      Arguments.of("BRINSFORD_PRISON", "Brinsford Prison"),
      Arguments.of("BRISTOL_PRISON", "Bristol Prison"),
      Arguments.of("BRIXTON_PRISON", "Brixton Prison"),
      Arguments.of("BRONZEFIELD_PRISON", "Bronzefield Prison"),
      Arguments.of("BUCKLEY_HALL_PRISON", "Buckley Hall Prison"),
      Arguments.of("BULLINGDON_PRISON", "Bullingdon Prison"),
      Arguments.of("BURE_PRISON", "Bure Prison"),
      Arguments.of("CARDIFF_PRISON", "Cardiff Prison"),
      Arguments.of("CHANNINGS_WOOD_PRISON", "Channings Wood Prison"),
      Arguments.of("CHELMSFORD_PRISON", "Chelmsford Prison"),
      Arguments.of("COLDINGLEY_PRISON", "Coldingley Prison"),
      Arguments.of("COOKHAM_WOOD_YOUNG_OFFENDER_INSTITUTION", "Cookham Wood Young Offender Institution"),
      Arguments.of("DARTMOOR_PRISON", "Dartmoor Prison"),
      Arguments.of("DEERBOLT_PRISON", "Deerbolt Prison"),
      Arguments.of("DONCASTER_PRISON", "Doncaster Prison"),
      Arguments.of("DOVEGATE_PRISON", "Dovegate Prison"),
      Arguments.of("DOWNVIEW_PRISON_AND_YOUNG_OFFENDER_INSTITUTION", "Downview Prison and Young Offender Institution"),
      Arguments.of(
        "DRAKE_HALL_PRISON_AND_YOUNG_OFFENDER_INSTITUTION",
        "Drake Hall Prison and Young Offender Institution",
      ),
      Arguments.of("DURHAM_PRISON", "Durham Prison"),
      Arguments.of(
        "EAST_SUTTON_PARK_PRISON_AND_YOUNG_OFFENDERS_INSTITUTION",
        "East Sutton Park Prison and Young Offender Institution",
      ),
      Arguments.of(
        "EASTWOOD_PARK_PRISON_AND_YOUNG_OFFENDER_INSTITUTION",
        "Eastwood Park Prison and Young Offender Institution",
      ),
      Arguments.of("ELMLEY_PRISON", "Elmley Prison"),
      Arguments.of("ERLESTOKE_PRISON", "Erlestoke Prison"),
      Arguments.of("EXETER_PRISON", "Exeter Prison"),
      Arguments.of("FEATHERSTONE_PRISON", "Featherstone Prison"),
      Arguments.of("FELTHAM_YOUNG_OFFENDER_INSTITUTION", "Feltham Young Offender Institution"),
      Arguments.of("FIVE_WELLS_PRISON", "Five Wells Prison"),
      Arguments.of("FORD_PRISON", "Ford Prison"),
      Arguments.of("FOREST_BANK_PRISON", "Forest Bank Prison"),
      Arguments.of("FOSSE_WAY_PRISON", "Fosse Way Prison"),
      Arguments.of(
        "FOSTON_HALL_PRISON_AND_YOUNG_OFFENDER_INSTITUTION",
        "Foston Hall Prison and Young Offender Institution",
      ),
      Arguments.of("FRANKLAND_PRISON", "Frankland Prison"),
      Arguments.of("FULL_SUTTON_PRISON", "Full Sutton Prison"),
      Arguments.of("GARTH_PRISON", "Garth Prison"),
      Arguments.of("GARTREE_PRISON", "Gartree Prison"),
      Arguments.of("GRENDON_PRISON", "Grendon Prison"),
      Arguments.of("GUYS_MARSH_PRISON", "Guys Marsh Prison"),
      Arguments.of("HATFIELD_PRISON", "Hatfield Prison"),
      Arguments.of("HAVERIGG_PRISON", "Haverigg Prison"),
      Arguments.of("HEWELL_PRISON", "Hewell Prison"),
      Arguments.of("HIGH_DOWN_PRISON", "High Down Prison"),
      Arguments.of("HIGHPOINT_PRISON", "Highpoint Prison"),
      Arguments.of("HINDLEY_PRISON", "Hindley Prison"),
      Arguments.of("HOLLESLEY_BAY_PRISON", "Hollesley Bay Prison"),
      Arguments.of("HOLME_HOUSE_PRISON", "Holme House Prison"),
      Arguments.of("HULL_PRISON", "Hull Prison"),
      Arguments.of("HUMBER_PRISON", "Humber Prison"),
      Arguments.of("HUNTERCOMBE_PRISON", "Huntercombe Prison"),
      Arguments.of("ISIS_PRISON", "Isis Prison"),
      Arguments.of("ISLE_OF_WIGHT_PRISON", "Isle of Wight Prison"),
      Arguments.of("KIRKHAM_PRISON", "Kirkham Prison"),
      Arguments.of("KIRKLEVINGTON_GRANGE_PRISON", "Kirklevington Grange Prison"),
      Arguments.of("LANCASTER_FARMS_PRISON", "Lancaster Farms Prison"),
      Arguments.of("LEEDS_PRISON", "Leeds Prison"),
      Arguments.of("LEICESTER_PRISON", "Leicester Prison"),
      Arguments.of("LEWES_PRISON", "Lewes Prison"),
      Arguments.of("LEYHILL_PRISON", "Leyhill Prison"),
      Arguments.of("LINCOLN_PRISON", "Lincoln Prison"),
      Arguments.of("LINDHOLME_PRISON", "Lindholme Prison"),
      Arguments.of("LITTLEHEY_PRISON", "Littlehey Prison"),
      Arguments.of("LIVERPOOL_PRISON", "Liverpool Prison"),
      Arguments.of("LONG_LARTIN_PRISON", "Long Lartin Prison"),
      Arguments.of(
        "LOW_NEWTON_PRISON_AND_YOUNG_OFFENDER_INSTITUTION",
        "Low Newton Prison and Young Offender Institution",
      ),
      Arguments.of("LOWDHAM_GRANGE_PRISON", "Lowdham Grange Prison"),
      Arguments.of("MAIDSTONE_PRISON", "Maidstone Prison"),
      Arguments.of("MANCHESTER_PRISON", "Manchester Prison"),
      Arguments.of("MOORLAND_PRISON", "Moorland Prison"),
      Arguments.of("MORTON_HALL_PRISON", "Morton Hall Prison"),
      Arguments.of("NEW_HALL_PRISON_AND_YOUNG_OFFENDER_INSTITUTION", "New Hall Prison and Young Offender Institution"),
      Arguments.of("NORTH_SEA_CAMP_PRISON", "North Sea Camp Prison"),
      Arguments.of("NORTHUMBERLAND_PRISON", "Northumberland Prison"),
      Arguments.of("NORWICH_PRISON", "Norwich Prison"),
      Arguments.of("NOTTINGHAM_PRISON", "Nottingham Prison"),
      Arguments.of("OAKWOOD_PRISON", "Oakwood Prison"),
      Arguments.of("ONLEY_PRISON", "Onley Prison"),
      Arguments.of("PARC_PRISON_AND_YOUNG_OFFENDER_INSTITUTION", "Parc Prison and Young Offender Institute"),
      Arguments.of("PENTONVILLE_PRISON", "Pentonville Prison"),
      Arguments.of("PETERBOROUGH_PRISON", "Peterborough Prison"),
      Arguments.of("PORTLAND_PRISON_AND_YOUNG_OFFENDER_INSTITUTION", "Portland Prison and Young Offender Institution"),
      Arguments.of("PRESCOED_PRISON", "Prescoed Prison"),
      Arguments.of("PRESTON_PRISON", "Preston Prison"),
      Arguments.of("RANBY_PRISON", "Ranby Prison"),
      Arguments.of("RISLEY_PRISON", "Risley Prison"),
      Arguments.of("ROCHESTER_PRISON", "Rochester Prison"),
      Arguments.of("RYE_HILL_PRISON", "Rye Hill Prison"),
      Arguments.of("SEND_PRISON", "Send Prison"),
      Arguments.of("SPRING_HILL_PRISON", "Spring Hill Prison"),
      Arguments.of("STAFFORD_PRISON", "Stafford Prison"),
      Arguments.of("STANDFORD_HILL_PRISON", "Standford Hill Prison"),
      Arguments.of("STOCKEN_PRISON", "Stocken Prison"),
      Arguments.of("STOKE_HEATH_PRISON", "Stoke Heath Prison"),
      Arguments.of("STYAL_PRISON_AND_YOUNG_OFFENDER_INSTITUTION", "Styal Prison and Young Offender Institution"),
      Arguments.of("SUDBURY_PRISON", "Sudbury Prison"),
      Arguments.of("SWALESIDE_PRISON", "Swaleside Prison"),
      Arguments.of("SWANSEA_PRISON", "Swansea Prison"),
      Arguments.of("SWINFEN_HALL_PRISON", "Swinfen Hall Prison"),
      Arguments.of("THAMESIDE_PRISON", "Thameside Prison"),
      Arguments.of("THE_MOUNT_PRISON", "The Mount Prison"),
      Arguments.of("THE_VERNE_PRISON", "The Verne Prison"),
      Arguments.of("THORN_CROSS_PRISON", "Thorn Cross Prison"),
      Arguments.of("USK_PRISON", "Usk Prison"),
      Arguments.of("WAKEFIELD_PRISON", "Wakefield Prison"),
      Arguments.of("WANDSWORTH_PRISON", "Wandsworth Prison"),
      Arguments.of("WARREN_HILL_PRISON", "Warren Hill Prison"),
      Arguments.of("WAYLAND_PRISON", "Wayland Prison"),
      Arguments.of("WEALSTUN_PRISON", "Wealstun Prison"),
      Arguments.of("WERRINGTON_YOUNG_OFFENDER_INSTITUTION", "Werrington Young Offender Institution"),
      Arguments.of("WETHERBY_YOUNG_OFFENDER_INSTITUTION", "Wetherby Young Offender Institution"),
      Arguments.of("WHATTON_PRISON", "Whatton Prison"),
      Arguments.of("WHITEMOOR_PRISON", "Whitemoor Prison"),
      Arguments.of("WINCHESTER_PRISON", "Winchester Prison"),
      Arguments.of("WOODHILL_PRISON", "Woodhill Prison"),
      Arguments.of("WORMWOOD_SCRUBS_PRISON", "Wormwood Scrubs Prison"),
      Arguments.of("WYMOTT_PRISON", "Wymott Prison"),
    )

    @JvmStatic
    fun getPilots() = listOf(
      Arguments.of("ACQUISITIVE_CRIME_PROJECT", "Acquisitive Crime Project"),
      Arguments.of("DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_PROJECT", "Domestic Abuse perpetrators on Licence Project"),
      Arguments.of("LICENCE_VARIATION_PROJECT", "Licence Variation Project"),
      Arguments.of("DOMESTIC_ABUSE_PROTECTION_ORDER", "Domestic Abuse Protection Order (DAPO)"),
      Arguments.of("DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_DAPOL", "Domestic Abuse Perpetrator on Licence (DAPOL)"),
      Arguments.of(
        "DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_HOME_DETENTION_CURFEW_DAPOL_HDC",
        "Domestic Abuse Perpetrator on Licence Home Detention Curfew (DAPOL HDC)",
      ),
      Arguments.of("GPS_ACQUISITIVE_CRIME_HOME_DETENTION_CURFEW", "GPS Acquisitive Crime Home Detention Curfew"),
      Arguments.of("GPS_ACQUISITIVE_CRIME_PAROLE", "GPS Acquisitive Crime Parole"),
      Arguments.of("UNKNOWN", ""),
    )

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
