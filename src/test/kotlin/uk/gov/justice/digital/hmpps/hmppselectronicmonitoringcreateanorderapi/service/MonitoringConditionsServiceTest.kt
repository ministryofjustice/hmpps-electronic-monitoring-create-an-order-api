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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
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
      service.updateMonitoringConditions(mockOrderId, mockUsername, UpdateMonitoringConditionsDto(pilot = "some pilot"))

    assertThat(mockOrder.monitoringConditions).isNotNull
    assertThat(mockOrder.monitoringConditions!!.pilot).isEqualTo("some pilot")
    assertThat(result.pilot).isEqualTo("some pilot")
  }

  @Test
  fun `Should clear tag at source details if tag at source is not available`() {
    mockVersion.installationLocation =
      InstallationLocation(versionId = mockVersion.id, location = InstallationLocationType.PRISON)
    mockVersion.installationAppointment =
      InstallationAppointment(
        versionId = mockVersion.id,
        placeName = "MockPlace",
        appointmentDate = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(2),
      )
    mockVersion.monitoringConditionsAlcohol = AlcoholMonitoringConditions(versionId = mockVersion.id)
    mockVersion.addresses =
      mutableListOf(
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

    val result =
      service.updateMonitoringConditions(mockOrderId, mockUsername, UpdateMonitoringConditionsDto(alcohol = false))

    assertThat(mockOrder.monitoringConditions).isNotNull
    assertThat(mockOrder.installationLocation).isNull()
    assertThat(mockOrder.installationAppointment).isNull()
    assertThat(mockOrder.monitoringConditionsAlcohol).isNull()
    assertThat(mockOrder.addresses.firstOrNull { it.addressType == AddressType.INSTALLATION }).isNull()
  }
}
