package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Zone

@ActiveProfiles("test")
class MonitoringOrderTest : FmsTestBase() {

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

  companion object {
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
  }
}
