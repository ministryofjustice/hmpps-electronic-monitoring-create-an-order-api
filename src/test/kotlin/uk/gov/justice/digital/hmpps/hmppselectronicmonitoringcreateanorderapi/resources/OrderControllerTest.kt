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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
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

  private val mockDictionaryVersion = DataDictionaryVersion.DDV4

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
          dataDictionaryVersion = mockDictionaryVersion,
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
          dataDictionaryVersion = mockDictionaryVersion,
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
        fmsResultDate = null,
        installationAndRisk = null,
        interestedParties = null,
        isValid = false,
        mandatoryAttendanceConditions = mutableListOf(),
        monitoringConditions = null,
        monitoringConditionsAlcohol = null,
        monitoringConditionsTrail = null,
        status = OrderStatus.IN_PROGRESS,
        type = RequestType.REQUEST,
        username = "mockUser",
        variationDetails = null,
        probationDeliveryUnit = null,
        installationLocation = null,
        installationAppointment = null,
        dataDictionaryVersion = mockDictionaryVersion,
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
            dataDictionaryVersion = mockDictionaryVersion,
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
            dataDictionaryVersion = mockDictionaryVersion,
          ),
        ),
      ),
    )

    `when`(orderService.listOrders(OrderListCriteria(username = "mockUser"))).thenReturn(orders)
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
          fmsResultDate = null,
          installationAndRisk = null,
          interestedParties = null,
          isValid = false,
          mandatoryAttendanceConditions = mutableListOf(),
          monitoringConditions = null,
          monitoringConditionsAlcohol = null,
          monitoringConditionsTrail = null,
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          username = "mockUser",
          variationDetails = null,
          probationDeliveryUnit = null,
          installationLocation = null,
          installationAppointment = null,
          dataDictionaryVersion = mockDictionaryVersion,
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
          fmsResultDate = null,
          installationAndRisk = null,
          interestedParties = null,
          isValid = false,
          mandatoryAttendanceConditions = mutableListOf(),
          monitoringConditions = null,
          monitoringConditionsAlcohol = null,
          monitoringConditionsTrail = null,
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          username = "mockUser",
          variationDetails = null,
          probationDeliveryUnit = null,
          installationLocation = null,
          installationAppointment = null,
          dataDictionaryVersion = mockDictionaryVersion,
        ),
      ),
    )
  }

  @Test
  fun `Search for orders given a full name`() {
    val orderId = UUID.randomUUID()
    val orderId2 = UUID.randomUUID()
    val orders: List<Order> = listOf(
      Order(
        id = orderId,
        versions = mutableListOf(
          OrderVersion(
            orderId = orderId,
            username = "mockUser",
            status = OrderStatus.SUBMITTED,
            type = RequestType.REQUEST,
            dataDictionaryVersion = mockDictionaryVersion,
          ),
        ),
      ),
      Order(
        id = orderId2,
        versions = mutableListOf(
          OrderVersion(
            orderId = orderId2,
            username = "mockUser",
            status = OrderStatus.SUBMITTED,
            type = RequestType.REQUEST,
            dataDictionaryVersion = mockDictionaryVersion,
          ),
        ),
      ),
    )

    `when`(orderService.searchOrders(OrderSearchCriteria(searchTerm = "Bob Smith"))).thenReturn(orders)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.searchOrders("Bob Smith", authentication)
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
          fmsResultDate = null,
          installationAndRisk = null,
          interestedParties = null,
          isValid = false,
          mandatoryAttendanceConditions = mutableListOf(),
          monitoringConditions = null,
          monitoringConditionsAlcohol = null,
          monitoringConditionsTrail = null,
          status = OrderStatus.SUBMITTED,
          type = RequestType.REQUEST,
          username = "mockUser",
          variationDetails = null,
          probationDeliveryUnit = null,
          installationLocation = null,
          installationAppointment = null,
          dataDictionaryVersion = mockDictionaryVersion,
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
          fmsResultDate = null,
          installationAndRisk = null,
          interestedParties = null,
          isValid = false,
          mandatoryAttendanceConditions = mutableListOf(),
          monitoringConditions = null,
          monitoringConditionsAlcohol = null,
          monitoringConditionsTrail = null,
          status = OrderStatus.SUBMITTED,
          type = RequestType.REQUEST,
          username = "mockUser",
          variationDetails = null,
          probationDeliveryUnit = null,
          installationLocation = null,
          installationAppointment = null,
          dataDictionaryVersion = mockDictionaryVersion,
        ),
      ),
    )
  }
}
