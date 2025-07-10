package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.*

class MonitoringConditionsServiceTest {

  lateinit var repo: OrderRepository

  lateinit var service: MonitoringConditionsService

  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockUsername: String = "username"
  private val mockOrder = Order(
    id = mockOrderId,
    versions = mutableListOf(
      OrderVersion(
        versionId = 0,
        status = OrderStatus.IN_PROGRESS,
        orderId = mockOrderId,
        type = RequestType.REQUEST,
        username = mockUsername,
        dataDictionaryVersion = DataDictionaryVersion.DDV4,
      ),
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
}
