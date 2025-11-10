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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class MonitoringConditionsServiceTest {

  lateinit var repo: OrderRepository

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
    service = MonitoringConditionsService(tagAtSourceEnabled = false)
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
  fun `Should clear tag at source details if tag at source is not available`() {
    mockVersion.installationLocation =
      InstallationLocation(versionId = mockVersion.id, location = InstallationLocationType.PRISON)
    mockVersion.installationAppointment = InstallationAppointment(
      versionId = mockVersion.id,
      placeName = "MockPlace",
      appointmentDate = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(2),
    )
    mockVersion.monitoringConditionsAlcohol = AlcoholMonitoringConditions(versionId = mockVersion.id)
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

    service.updateMonitoringConditions(mockOrderId, mockUsername, UpdateMonitoringConditionsDto(alcohol = false))

    assertThat(mockOrder.monitoringConditions).isNotNull
    assertThat(mockOrder.installationLocation).isNull()
    assertThat(mockOrder.installationAppointment).isNull()
    assertThat(mockOrder.monitoringConditionsAlcohol).isNull()
    assertThat(mockOrder.addresses.firstOrNull { it.addressType == AddressType.INSTALLATION }).isNull()
  }

  @Test
  fun `When Tag at Source is ENABLED, should populate tag at source details for non-alcohol order`() {
    service = MonitoringConditionsService(tagAtSourceEnabled = true)
    service.orderRepo = repo

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

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringCondition(
      mockOrderId,
      mockUsername,
      monitoringConditionId = curfewId,
    )

    assertThat(mockOrder.curfewConditions).isNull()
    assertThat(mockOrder.curfewTimeTable.isEmpty()).isEqualTo(true)
    assertThat(mockOrder.curfewReleaseDateConditions).isNull()
  }

  @Test
  fun `can delete curfew by referencing curfew release date conditions`() {
    val curfewId = UUID.randomUUID()
    mockOrder.curfewConditions = CurfewConditions(versionId = UUID.randomUUID())
    mockOrder.curfewTimeTable =
      mutableListOf(CurfewTimeTable(versionId = UUID.randomUUID(), dayOfWeek = DayOfWeek.MONDAY))
    mockOrder.curfewReleaseDateConditions = CurfewReleaseDateConditions(id = curfewId, versionId = UUID.randomUUID())

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringCondition(
      mockOrderId,
      mockUsername,
      monitoringConditionId = curfewId,
    )

    assertThat(mockOrder.curfewConditions).isNull()
    assertThat(mockOrder.curfewTimeTable.isEmpty()).isEqualTo(true)
    assertThat(mockOrder.curfewReleaseDateConditions).isNull()
  }

  @Test
  fun `can delete curfew by referencing curfew any curfew timetable `() {
    val curfewId = UUID.randomUUID()
    mockOrder.curfewConditions = CurfewConditions(versionId = UUID.randomUUID())
    mockOrder.curfewTimeTable =
      mutableListOf(CurfewTimeTable(id = curfewId, versionId = UUID.randomUUID(), dayOfWeek = DayOfWeek.MONDAY))
    mockOrder.curfewReleaseDateConditions = CurfewReleaseDateConditions(versionId = UUID.randomUUID())

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringCondition(
      mockOrderId,
      mockUsername,
      monitoringConditionId = curfewId,
    )

    assertThat(mockOrder.curfewConditions).isNull()
    assertThat(mockOrder.curfewTimeTable.isEmpty()).isEqualTo(true)
    assertThat(mockOrder.curfewReleaseDateConditions).isNull()
  }

  @Test
  fun `can delete trail`() {
    val trailID = UUID.randomUUID()
    mockOrder.monitoringConditionsTrail = TrailMonitoringConditions(id = trailID, versionId = UUID.randomUUID())

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringCondition(
      mockOrderId,
      mockUsername,
      monitoringConditionId = trailID,
    )

    assertThat(mockOrder.monitoringConditionsTrail).isNull()
  }

  @Test
  fun `can delete alcohol`() {
    val alcoholId = UUID.randomUUID()
    mockOrder.monitoringConditionsAlcohol = AlcoholMonitoringConditions(id = alcoholId, versionId = UUID.randomUUID())

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.removeMonitoringCondition(
      mockOrderId,
      mockUsername,
      monitoringConditionId = alcoholId,
    )

    assertThat(mockOrder.monitoringConditionsAlcohol).isNull()
  }
}
