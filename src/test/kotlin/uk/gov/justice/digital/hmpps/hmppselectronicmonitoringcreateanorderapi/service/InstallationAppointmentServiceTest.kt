package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationAppointmentDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
@JsonTest
class InstallationAppointmentServiceTest {

  lateinit var repo: OrderRepository

  lateinit var service: InstallationAppointmentService

  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockVersionId: UUID = UUID.randomUUID()
  private val mockUsername: String = "username"
  private val mockOrder = Order(
    id = mockOrderId,
    versions = mutableListOf(
      OrderVersion(
        id = mockVersionId,
        versionId = 0,
        status = OrderStatus.IN_PROGRESS,
        orderId = mockOrderId,
        type = RequestType.REQUEST,
        username = mockUsername,

      ),
    ),
  )

  @BeforeEach
  fun setup() {
    repo = Mockito.mock(OrderRepository::class.java)
    service = InstallationAppointmentService()
    service.orderRepo = repo
  }

  @Test
  fun `Should throw exception when order is not found`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.empty())

    Assertions.assertThrows(EntityNotFoundException::class.java) {
      service.createOrUpdateInstallationAppointment(
        mockOrderId,
        mockUsername,
        UpdateInstallationAppointmentDto(placeName = "Mock Place", appointmentDate = ZonedDateTime.now()),
      )
    }
  }

  @Test
  fun `Should throw exception when order is already submitted`() {
    mockOrder.status = OrderStatus.SUBMITTED
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))

    Assertions.assertThrows(EntityNotFoundException::class.java) {
      service.createOrUpdateInstallationAppointment(
        mockOrderId,
        mockUsername,
        UpdateInstallationAppointmentDto(placeName = "Mock Place", appointmentDate = ZonedDateTime.now()),
      )
    }
  }

  @Test
  fun `Should throw exception when order is not created by the user`() {
    mockOrder.versions = mutableListOf(
      OrderVersion(
        id = mockVersionId,
        versionId = 0,
        status = OrderStatus.IN_PROGRESS,
        orderId = mockOrderId,
        type = RequestType.REQUEST,
        username = "Not$mockUsername",

      ),
    )
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))

    Assertions.assertThrows(EntityNotFoundException::class.java) {
      service.createOrUpdateInstallationAppointment(
        mockOrderId,
        mockUsername,
        UpdateInstallationAppointmentDto(placeName = "Mock Place", appointmentDate = ZonedDateTime.now()),
      )
    }
  }

  @Test
  fun `Should update order with new installation location and return`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    val updateRecord = UpdateInstallationAppointmentDto(placeName = "Mock Place", appointmentDate = ZonedDateTime.now())
    val result = service.createOrUpdateInstallationAppointment(
      mockOrderId,
      mockUsername,
      updateRecord,
    )

    assertThat(mockOrder.installationAppointment).isNotNull
    assertThat(
      mockOrder.installationAppointment!!.placeName,
    ).isEqualTo(updateRecord.placeName)
    assertThat(
      mockOrder.installationAppointment!!.appointmentDate,
    ).isEqualTo(updateRecord.appointmentDate)
    assertThat(result.placeName).isEqualTo(updateRecord.placeName)
    assertThat(result.appointmentDate).isEqualTo(updateRecord.appointmentDate)
  }
}
