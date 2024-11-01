package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmitFmsOrderResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.*

@ActiveProfiles("test")
@JsonTest
class OrderServiceTest {
  private lateinit var repo: OrderRepository
  private lateinit var fmsService: FmsService
  private lateinit var service: OrderService

  @BeforeEach
  fun setup() {
    repo = mock(OrderRepository::class.java)
    fmsService = mock(FmsService::class.java)
    service = OrderService(repo, fmsService)
  }

  @Test
  fun `Create a new order for user and save to database`() {
    val result = service.createOrder("mockUser")

    Assertions.assertThat(result.id).isNotNull()
    Assertions.assertThat(UUID.fromString(result.id.toString())).isEqualTo(result.id)
    Assertions.assertThat(result.username).isEqualTo("mockUser")
    Assertions.assertThat(result.status).isEqualTo(OrderStatus.IN_PROGRESS)
    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue).isEqualTo(result)
    }
  }

  @Test
  fun `Should create fms device wearer and monitoring order and save both id to database`() {
    val mockOrder = Order(
      username = "mockUser",
      status = OrderStatus.IN_PROGRESS,
    )

    whenever(repo.findByUsernameAndId("mockUser", mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(fmsService.submitOrder(any<Order>())).thenReturn(
      SubmitFmsOrderResult(
        deviceWearerId = "mockDeviceWearerId",
        fmsOrderId = "mockMonitoringOrderId",
      ),
    )
    service.submitOrder(mockOrder.id, "mockUser")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue.fmsDeviceWearerId).isEqualTo("mockDeviceWearerId")
      Assertions.assertThat(firstValue.fmsMonitoringOrderId).isEqualTo("mockMonitoringOrderId")
    }
  }
}
