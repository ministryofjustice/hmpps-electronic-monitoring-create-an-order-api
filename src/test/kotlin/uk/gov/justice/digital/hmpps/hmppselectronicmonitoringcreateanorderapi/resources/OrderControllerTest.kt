package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resources

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.OrderController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OrderService
import java.util.*

@ActiveProfiles("test")
@JsonTest
class OrderControllerTest {
  private val orderService: OrderService = mock()
  private val controller = OrderController(orderService)
  private lateinit var authentication: Authentication

  @BeforeEach
  fun setup() {
    authentication = mock(Authentication::class.java)
  }

  @Test
  fun `Create a new order and return`() {
    val mockOrder = Order(username = "mockUser", status = OrderStatus.IN_PROGRESS, type = OrderType.REQUEST)
    `when`(orderService.createOrder("mockUser", CreateOrderDto())).thenReturn(mockOrder)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.createOrder(authentication, CreateOrderDto())
    Assertions.assertThat(result.body).isNotNull
    Assertions.assertThat(result.body!!.username).isEqualTo("mockUser")
    Assertions.assertThat(result.body!!.status).isEqualTo(OrderStatus.IN_PROGRESS)
    Assertions.assertThat(result.body!!.id).isNotNull()
    Assertions.assertThat(UUID.fromString(result.body!!.id.toString())).isEqualTo(result.body!!.id)
  }

  @Test
  fun `Query a single order and return`() {
    val order = Order(username = "mockUser", status = OrderStatus.IN_PROGRESS, type = OrderType.REQUEST)

    `when`(orderService.getOrder("mockUser", order.id)).thenReturn(order)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.getOrder(order.id, authentication)
    Assertions.assertThat(result.body).isEqualTo(order)
  }

  @Test
  fun `Query orders for current user and return`() {
    val orders: List<Order> = listOf(
      Order(username = "mockUser", status = OrderStatus.IN_PROGRESS, type = OrderType.REQUEST),
      Order(username = "mockUser", status = OrderStatus.IN_PROGRESS, type = OrderType.REQUEST),
    )

    `when`(orderService.listOrders(OrderSearchCriteria(username = "mockUser"))).thenReturn(orders)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.listOrders("", authentication)
    Assertions.assertThat(result.body).isEqualTo(orders)
  }
}
