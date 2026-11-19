package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MandatoryAttendanceConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SentenceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class MonitoringConditionsServiceTest : OrderSectionServiceTestBase() {
  lateinit var service: MonitoringConditionsService

  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockUsername: String = "username"
  private val mockVersion = OrderVersion(
    versionId = 0,
    status = OrderStatus.IN_PROGRESS,
    orderId = mockOrderId,
    type = RequestType.REQUEST,
    username = mockUsername,
    dataDictionaryVersion = DataDictionaryVersion.DDV4,
  )
  private val mockOrder = Order(
    id = mockOrderId,
    versions = mutableListOf(
      mockVersion,
    ),
  )

  @BeforeEach
  fun setup() {
    repo = mock(OrderRepository::class.java)
    service = MonitoringConditionsService()
    service.orderRepo = repo
  }

  @Test
  fun `Should throw exception when order is not found`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      service.updateMonitoringConditions(
        mockOrderId,
        mockUsername,
        UpdateMonitoringConditionsDto(),
      )
    }
  }

  @Test
  fun `Should throw exception if order is already submitted`() {
    mockOrder.status = OrderStatus.SUBMITTED
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))

    assertThrows<EntityNotFoundException> {
      service.updateMonitoringConditions(
        mockOrderId,
        mockUsername,
        UpdateMonitoringConditionsDto(),
      )
    }
  }

  @Test
  fun `Should be able to update the monitoring conditions pilot`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    val result =
      service.updateMonitoringConditions(
        mockOrderId,
        mockUsername,
        UpdateMonitoringConditionsDto(
          pilot = Pilot.DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_HOME_DETENTION_CURFEW_DAPOL_HDC,
        ),
      )

    assertThat(mockOrder.monitoringConditions).isNotNull
    assertThat(
      mockOrder.monitoringConditions!!.pilot,
    ).isEqualTo(Pilot.DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_HOME_DETENTION_CURFEW_DAPOL_HDC)
    assertThat(result.pilot).isEqualTo(Pilot.DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_HOME_DETENTION_CURFEW_DAPOL_HDC)
  }

  @Test
  fun `Should retain tag at source details for non-alcohol order`() {
    mockVersion.installationLocation =
      InstallationLocation(versionId = mockVersion.id, location = InstallationLocationType.PRISON)
    mockVersion.installationAppointment = InstallationAppointment(
      versionId = mockVersion.id,
      placeName = "MockPlace",
      appointmentDate = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(2),
    )
    mockVersion.monitoringConditionsTrail = TrailMonitoringConditions(versionId = mockVersion.id)
    mockVersion.addresses = mutableListOf(
      Address(
        versionId = mockVersion.id,
        addressType = AddressType.INSTALLATION,
        addressLine1 = "Mock place",
        addressLine2 = "",
        addressLine3 = "Mock Town",
        postcode = "Mock postcode",
      ),
    )
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.updateMonitoringConditions(mockOrderId, mockUsername, UpdateMonitoringConditionsDto(trail = true))

    assertThat(mockOrder.monitoringConditions).isNotNull
    assertThat(mockOrder.monitoringConditionsTrail).isNotNull
    assertThat(mockOrder.installationLocation).isNotNull
    assertThat(mockOrder.installationAppointment).isNotNull
    assertThat(mockOrder.addresses.firstOrNull { it.addressType == AddressType.INSTALLATION }).isNotNull
  }

  @Test
  fun `should clear alcohol monitoring details when alcohol is deselected`() {
    mockVersion.monitoringConditionsAlcohol = AlcoholMonitoringConditions(versionId = mockVersion.id)
    assertThat(mockOrder.monitoringConditionsAlcohol).isNotNull()

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.updateMonitoringConditions(
      mockOrderId,
      mockUsername,
      UpdateMonitoringConditionsDto(alcohol = false, trail = true),
    )

    assertThat(mockOrder.monitoringConditionsAlcohol).isNull()
  }

  @Test
  fun `should clear all curfew details when curfew is deselected`() {
    mockVersion.curfewConditions = CurfewConditions(versionId = mockVersion.id)
    mockVersion.curfewReleaseDateConditions = CurfewReleaseDateConditions(versionId = mockVersion.id)
    mockVersion.curfewTimeTable =
      mutableListOf(CurfewTimeTable(versionId = mockVersion.id, dayOfWeek = DayOfWeek.MONDAY))

    assertThat(mockOrder.curfewConditions).isNotNull()
    assertThat(mockOrder.curfewReleaseDateConditions).isNotNull()
    assertThat(mockOrder.curfewTimeTable).isNotEmpty()

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.updateMonitoringConditions(
      mockOrderId,
      mockUsername,
      UpdateMonitoringConditionsDto(curfew = false, trail = true),
    )

    assertThat(mockOrder.curfewConditions).isNull()
    assertThat(mockOrder.curfewReleaseDateConditions).isNull()
    assertThat(mockOrder.curfewTimeTable).isEmpty()
  }

  @Test
  fun `should clear trail monitoring details when trail is deselected`() {
    mockVersion.monitoringConditionsTrail = TrailMonitoringConditions(versionId = mockVersion.id)
    assertThat(mockOrder.monitoringConditionsTrail).isNotNull()

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.updateMonitoringConditions(
      mockOrderId,
      mockUsername,
      UpdateMonitoringConditionsDto(trail = false, alcohol = true),
    )

    assertThat(mockOrder.monitoringConditionsTrail).isNull()
  }

  @Test
  fun `should clear enforcement zone conditions when exclusionZone is deselected`() {
    mockVersion.enforcementZoneConditions = mutableListOf(
      EnforcementZoneConditions(versionId = mockVersion.id, zoneType = EnforcementZoneType.EXCLUSION),
    )
    assertThat(mockOrder.enforcementZoneConditions).isNotEmpty()

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.updateMonitoringConditions(
      mockOrderId,
      mockUsername,
      UpdateMonitoringConditionsDto(exclusionZone = false, alcohol = true),
    )

    assertThat(mockOrder.enforcementZoneConditions).isEmpty()
  }

  @Test
  fun `should clear mandatory attendance conditions when mandatoryAttendance is deselected`() {
    mockVersion.mandatoryAttendanceConditions = mutableListOf(MandatoryAttendanceConditions(versionId = mockVersion.id))
    assertThat(mockOrder.mandatoryAttendanceConditions).isNotEmpty()

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.updateMonitoringConditions(
      mockOrderId,
      mockUsername,
      UpdateMonitoringConditionsDto(mandatoryAttendance = false, alcohol = true),
    )

    assertThat(mockOrder.mandatoryAttendanceConditions).isEmpty()
  }

  @Test
  fun `can delete curfew by referencing curfew conditions`() {
    val curfewId = UUID.randomUUID()
    mockOrder.curfewConditions = CurfewConditions(id = curfewId, versionId = UUID.randomUUID())
    mockOrder.curfewTimeTable =
      mutableListOf(CurfewTimeTable(versionId = UUID.randomUUID(), dayOfWeek = DayOfWeek.MONDAY))
    mockOrder.curfewReleaseDateConditions = CurfewReleaseDateConditions(versionId = UUID.randomUUID())
    mockOrder.monitoringConditions = MonitoringConditions(versionId = UUID.randomUUID(), curfew = true)

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringType(
      mockOrderId,
      mockUsername,
      monitoringTypeId = curfewId,
    )

    assertThat(mockOrder.curfewConditions).isNull()
    assertThat(mockOrder.curfewTimeTable.isEmpty()).isEqualTo(true)
    assertThat(mockOrder.curfewReleaseDateConditions).isNull()
    assertThat(mockOrder.monitoringConditions?.curfew).isFalse()
  }

  @Test
  fun `can delete curfew by referencing curfew release date conditions`() {
    val curfewId = UUID.randomUUID()
    mockOrder.curfewConditions = CurfewConditions(versionId = UUID.randomUUID())
    mockOrder.curfewTimeTable =
      mutableListOf(CurfewTimeTable(versionId = UUID.randomUUID(), dayOfWeek = DayOfWeek.MONDAY))
    mockOrder.curfewReleaseDateConditions = CurfewReleaseDateConditions(id = curfewId, versionId = UUID.randomUUID())
    mockOrder.monitoringConditions = MonitoringConditions(versionId = UUID.randomUUID(), curfew = true)

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringType(
      mockOrderId,
      mockUsername,
      monitoringTypeId = curfewId,
    )

    assertThat(mockOrder.curfewConditions).isNull()
    assertThat(mockOrder.curfewTimeTable.isEmpty()).isEqualTo(true)
    assertThat(mockOrder.curfewReleaseDateConditions).isNull()
    assertThat(mockOrder.monitoringConditions?.curfew).isFalse()
  }

  @Test
  fun `can delete curfew by referencing curfew any curfew timetable `() {
    val curfewId = UUID.randomUUID()
    mockOrder.curfewConditions = CurfewConditions(versionId = UUID.randomUUID())
    mockOrder.curfewTimeTable =
      mutableListOf(CurfewTimeTable(id = curfewId, versionId = UUID.randomUUID(), dayOfWeek = DayOfWeek.MONDAY))
    mockOrder.curfewReleaseDateConditions = CurfewReleaseDateConditions(versionId = UUID.randomUUID())
    mockOrder.monitoringConditions = MonitoringConditions(versionId = UUID.randomUUID(), curfew = true)

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringType(
      mockOrderId,
      mockUsername,
      monitoringTypeId = curfewId,
    )

    assertThat(mockOrder.curfewConditions).isNull()
    assertThat(mockOrder.curfewTimeTable.isEmpty()).isEqualTo(true)
    assertThat(mockOrder.curfewReleaseDateConditions).isNull()
    assertThat(mockOrder.monitoringConditions?.curfew).isFalse()
  }

  @Test
  fun `can delete trail`() {
    val trailID = UUID.randomUUID()
    mockOrder.monitoringConditionsTrail = TrailMonitoringConditions(id = trailID, versionId = UUID.randomUUID())
    mockOrder.monitoringConditions = MonitoringConditions(versionId = UUID.randomUUID(), trail = true)

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringType(
      mockOrderId,
      mockUsername,
      monitoringTypeId = trailID,
    )

    assertThat(mockOrder.monitoringConditionsTrail).isNull()
    assertThat(mockOrder.monitoringConditions?.trail).isFalse()
  }

  @Test
  fun `can delete alcohol`() {
    val alcoholId = UUID.randomUUID()
    mockOrder.monitoringConditionsAlcohol = AlcoholMonitoringConditions(id = alcoholId, versionId = UUID.randomUUID())
    mockOrder.monitoringConditions = MonitoringConditions(versionId = UUID.randomUUID(), alcohol = true)

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringType(
      mockOrderId,
      mockUsername,
      monitoringTypeId = alcoholId,
    )

    assertThat(mockOrder.monitoringConditionsAlcohol).isNull()
    assertThat(mockOrder.monitoringConditions?.alcohol).isFalse()
  }

  @Test
  fun `can delete only exclusion zone`() {
    val exclusionZoneId = UUID.randomUUID()
    mockOrder.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        id = exclusionZoneId,
        versionId = UUID.randomUUID(),
        zoneId = 0,
      ),
    )
    mockOrder.monitoringConditions = MonitoringConditions(versionId = UUID.randomUUID(), exclusionZone = true)

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringType(
      mockOrderId,
      mockUsername,
      monitoringTypeId = exclusionZoneId,
    )

    assertThat(mockOrder.enforcementZoneConditions.isEmpty()).isEqualTo(true)
    assertThat(mockOrder.monitoringConditions?.exclusionZone).isFalse()
  }

  @Test
  fun `can remove one exclusion zone form the list`() {
    val exclusionZoneId = UUID.randomUUID()
    mockOrder.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        id = UUID.randomUUID(),
        versionId = UUID.randomUUID(),
        zoneId = 0,
      ),
    )
    mockOrder.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        id = exclusionZoneId,
        versionId = UUID.randomUUID(),
        zoneId = 1,
      ),
    )
    mockOrder.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        id = UUID.randomUUID(),
        versionId = UUID.randomUUID(),
        zoneId = 2,
      ),
    )
    mockOrder.monitoringConditions = MonitoringConditions(versionId = UUID.randomUUID(), exclusionZone = true)

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringType(
      mockOrderId,
      mockUsername,
      monitoringTypeId = exclusionZoneId,
    )

    assertThat(mockOrder.enforcementZoneConditions.size).isEqualTo(2)
    assertThat(mockOrder.enforcementZoneConditions.first().zoneId).isEqualTo(0)
    assertThat(mockOrder.enforcementZoneConditions.last().zoneId).isEqualTo(1)
    assertThat(mockOrder.monitoringConditions?.exclusionZone).isTrue()
  }

  @Test
  fun `can remove one mandatory attendance form the list`() {
    val mandatoryAttendanceId = UUID.randomUUID()
    mockOrder.mandatoryAttendanceConditions.add(
      MandatoryAttendanceConditions(
        id = UUID.randomUUID(),
        versionId = UUID.randomUUID(),
      ),
    )
    mockOrder.mandatoryAttendanceConditions.add(
      MandatoryAttendanceConditions(
        id = mandatoryAttendanceId,
        versionId = UUID.randomUUID(),
      ),
    )
    mockOrder.mandatoryAttendanceConditions.add(
      MandatoryAttendanceConditions(
        id = UUID.randomUUID(),
        versionId = UUID.randomUUID(),
      ),
    )
    mockOrder.monitoringConditions = MonitoringConditions(versionId = UUID.randomUUID(), mandatoryAttendance = true)

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringType(
      mockOrderId,
      mockUsername,
      monitoringTypeId = mandatoryAttendanceId,
    )

    assertThat(mockOrder.mandatoryAttendanceConditions.size).isEqualTo(2)
    assertThat(mockOrder.monitoringConditions?.mandatoryAttendance).isTrue()
  }

  @Test
  fun `can delete tag at source data`() {
    mockOrder.installationLocation = InstallationLocation(versionId = UUID.randomUUID())
    mockOrder.installationAppointment = InstallationAppointment(versionId = UUID.randomUUID())
    mockOrder.addresses.add(
      Address(
        versionId = UUID.randomUUID(),
        addressType = AddressType.INSTALLATION,
        addressLine1 = "line1",
        addressLine2 = "",
        addressLine3 = "line3",
        postcode = "postcode",
      ),
    )

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeTagAtSource(mockOrderId, mockUsername)
  }

  @Test
  fun `applyImportedConditions creates parent row, sets flags and recalculates dates`() {
    val start = ZonedDateTime.of(2026, 12, 1, 0, 0, 0, 0, ZoneId.of("Europe/London"))
    val end = start.plusYears(1)

    service.applyImportedConditions(
      mockOrder,
      listOf(
        CurfewConditions(versionId = mockOrder.versionId, startDate = start, endDate = end),
        TrailMonitoringConditions(versionId = mockOrder.versionId, startDate = start, endDate = end.plusDays(5)),
      ),
    )

    assertThat(mockOrder.monitoringConditions?.curfew).isTrue()
    assertThat(mockOrder.monitoringConditions?.trail).isTrue()
    assertThat(mockOrder.monitoringConditions?.alcohol).isNull()
    assertThat(mockOrder.curfewConditions).isNotNull
    assertThat(mockOrder.monitoringConditions?.startDate).isEqualTo(start)
    assertThat(mockOrder.monitoringConditions?.endDate).isEqualTo(end.plusDays(5))
  }

  @Test
  fun `applyImportedConditions keeps existing parent row and leaves unmentioned types alone`() {
    val existingTrail = TrailMonitoringConditions(versionId = mockOrder.versionId)
    mockOrder.monitoringConditions = MonitoringConditions(
      versionId = mockOrder.versionId,
      sentenceType = SentenceType.STANDARD_DETERMINATE_SENTENCE,
      trail = true,
    )
    mockOrder.monitoringConditionsTrail = existingTrail
    val parentId = mockOrder.monitoringConditions!!.id

    service.applyImportedConditions(
      mockOrder,
      listOf(CurfewConditions(versionId = mockOrder.versionId)),
    )

    assertThat(mockOrder.monitoringConditions?.id).isEqualTo(parentId)
    assertThat(mockOrder.monitoringConditions?.sentenceType).isEqualTo(SentenceType.STANDARD_DETERMINATE_SENTENCE)
    assertThat(mockOrder.monitoringConditions?.trail).isTrue()
    assertThat(mockOrder.monitoringConditionsTrail).isSameAs(existingTrail)
    assertThat(mockOrder.monitoringConditions?.curfew).isTrue()
  }

  @Test
  fun `applyImportedConditions updates an existing single condition in place`() {
    val existing = CurfewConditions(versionId = mockOrder.versionId, curfewAddress = "user entered")
    mockOrder.curfewConditions = existing
    val end = ZonedDateTime.of(2027, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/London"))

    service.applyImportedConditions(
      mockOrder,
      listOf(CurfewConditions(versionId = mockOrder.versionId, endDate = end, curfewDescription = "from cvl")),
    )

    assertThat(mockOrder.curfewConditions).isSameAs(existing)
    assertThat(existing.curfewAddress).isEqualTo("user entered")
    assertThat(existing.endDate).isEqualTo(end)
    assertThat(existing.curfewDescription).isEqualTo("from cvl")
  }

  @Test
  fun `applyImportedConditions uses the first alcohol condition when several are supplied`() {
    service.applyImportedConditions(
      mockOrder,
      listOf(
        AlcoholMonitoringConditions(
          versionId = mockOrder.versionId,
          monitoringType = AlcoholMonitoringType.ALCOHOL_LEVEL,
        ),
        AlcoholMonitoringConditions(
          versionId = mockOrder.versionId,
          monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
        ),
      ),
    )

    assertThat(mockOrder.monitoringConditionsAlcohol?.monitoringType).isEqualTo(AlcoholMonitoringType.ALCOHOL_LEVEL)
    assertThat(mockOrder.monitoringConditions?.alcohol).isTrue()
  }

  @Test
  fun `applyImportedConditions replaces enforcement zones and numbers them`() {
    mockOrder.enforcementZoneConditions.add(EnforcementZoneConditions(versionId = mockOrder.versionId, zoneId = 0))

    service.applyImportedConditions(
      mockOrder,
      listOf(
        EnforcementZoneConditions(versionId = mockOrder.versionId, zoneType = EnforcementZoneType.EXCLUSION),
        EnforcementZoneConditions(versionId = mockOrder.versionId, zoneType = EnforcementZoneType.RESTRICTION),
      ),
    )

    assertThat(mockOrder.enforcementZoneConditions).extracting<Int?> { it.zoneId }.containsExactly(0, 1)
    assertThat(mockOrder.monitoringConditions?.exclusionZone).isTrue()
  }

  @Test
  fun `applyImportedConditions does nothing for an empty list`() {
    service.applyImportedConditions(mockOrder, emptyList())

    assertThat(mockOrder.monitoringConditions).isNull()
  }
}
