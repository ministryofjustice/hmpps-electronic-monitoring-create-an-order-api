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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
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
    val orderId = UUID.randomUUID()
    val mockOrder = Order(
      id = orderId,
      versions = mutableListOf(
        OrderVersion(
          orderId = orderId,
          username = "mockUser",
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
        ),
      ),
    )
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
    val orderId = UUID.randomUUID()
    val order = Order(
      id = orderId,
      versions = mutableListOf(
        OrderVersion(
          orderId = orderId,
          username = "mockUser",
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
        ),
      ),
    )

    `when`(orderService.getOrder(order.id, "mockUser")).thenReturn(order)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.getOrder(order.id, authentication)
    Assertions.assertThat(result.body).isEqualTo(
      OrderDto(
        id = orderId,
        additionalDocuments = mutableListOf(),
        addresses = mutableListOf(),
        contactDetails = null,
        curfewConditions = null,
        curfewReleaseDateConditions = null,
        curfewTimeTable = mutableListOf(),
        deviceWearer = null,
        deviceWearerResponsibleAdult = null,
        enforcementZoneConditions = mutableListOf(),
        fmsResultId = null,
        installationAndRisk = null,
        interestedParties = null,
        isValid = false,
        monitoringConditions = null,
        monitoringConditionsAlcohol = null,
        monitoringConditionsTrail = null,
        status = OrderStatus.IN_PROGRESS,
        type = RequestType.REQUEST,
        username = "mockUser",
        variationDetails = null,
      ),
    )
  }

  @Test
  fun `Query orders for current user and return`() {
    val orderId = UUID.randomUUID()
    val orderId2 = UUID.randomUUID()
    val orders: List<Order> = listOf(
      Order(
        id = orderId,
        versions = mutableListOf(
          OrderVersion(
            orderId = orderId,
            username = "mockUser",
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
          ),
        ),
      ),
      Order(
        id = orderId2,
        versions = mutableListOf(
          OrderVersion(
            orderId = orderId2,
            username = "mockUser",
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
          ),
        ),
      ),
    )

    `when`(orderService.listOrders(OrderSearchCriteria(username = "mockUser"))).thenReturn(orders)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.listOrders("", authentication)
    Assertions.assertThat(result.body).isEqualTo(
      listOf(
        OrderDto(
          id = orderId,
          additionalDocuments = mutableListOf(),
          addresses = mutableListOf(),
          contactDetails = null,
          curfewConditions = null,
          curfewReleaseDateConditions = null,
          curfewTimeTable = mutableListOf(),
          deviceWearer = null,
          deviceWearerResponsibleAdult = null,
          enforcementZoneConditions = mutableListOf(),
          fmsResultId = null,
          installationAndRisk = null,
          interestedParties = null,
          isValid = false,
          monitoringConditions = null,
          monitoringConditionsAlcohol = null,
          monitoringConditionsTrail = null,
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          username = "mockUser",
          variationDetails = null,
        ),
        OrderDto(
          id = orderId2,
          additionalDocuments = mutableListOf(),
          addresses = mutableListOf(),
          contactDetails = null,
          curfewConditions = null,
          curfewReleaseDateConditions = null,
          curfewTimeTable = mutableListOf(),
          deviceWearer = null,
          deviceWearerResponsibleAdult = null,
          enforcementZoneConditions = mutableListOf(),
          fmsResultId = null,
          installationAndRisk = null,
          interestedParties = null,
          isValid = false,
          monitoringConditions = null,
          monitoringConditionsAlcohol = null,
          monitoringConditionsTrail = null,
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          username = "mockUser",
          variationDetails = null,
        ),
      ),
    )
  }
}
