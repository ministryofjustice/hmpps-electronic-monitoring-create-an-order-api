package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.UUID

@Service
class OrderService(
  val repo: OrderRepository,
  val fmsService: FmsService,

) {

  fun createOrder(username: String): Order {
    val order = Order(
      username = username,
      status = OrderStatus.IN_PROGRESS,
    )
    order.deviceWearer = DeviceWearer(orderId = order.id)
    order.addresses = mutableListOf()
    order.deviceWearerContactDetails = ContactDetails(orderId = order.id)
    order.monitoringConditions = MonitoringConditions(orderId = order.id)
    order.additionalDocuments = mutableListOf()
    order.enforcementZoneConditions = mutableListOf()
    order.installationAndRisk = InstallationAndRisk(orderId = order.id)
    repo.save(order)
    return order
  }

  fun submitOrder(id: UUID, username: String) {
    val order = getOrder(username, id)!!
    val result = fmsService.submitOrder(order)
    order.fmsDeviceWearerId = result.deviceWearerId
    order.fmsMonitoringOrderId = result.fmsOrderId
    repo.save(order)
  }

  fun listOrdersForUser(username: String): List<Order> {
    return repo.findByUsername(
      username,
    )
  }

  fun getOrder(username: String, id: UUID): Order? {
    return repo.findByUsernameAndId(
      username,
      id,
    ).orElseThrow {
      EntityNotFoundException("Order ($id) for $username not found")
    }
  }
}
