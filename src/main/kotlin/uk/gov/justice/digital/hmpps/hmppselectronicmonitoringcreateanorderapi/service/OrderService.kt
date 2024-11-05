package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SubmitOrderException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as FmsDeviceWearer

@Service
class OrderService(
  val repo: OrderRepository,
  val fmsClient: FmsClient,
) {

  fun createOrder(username: String): Order {
    val order = Order(
      username = username,
      status = OrderStatus.IN_PROGRESS,
    )
    order.deviceWearer = DeviceWearer(orderId = order.id)
    order.addresses = mutableListOf()
    order.deviceWearerContactDetails = DeviceWearerContactDetails(orderId = order.id)
    order.monitoringConditions = MonitoringConditions(orderId = order.id)
    order.additionalDocuments = mutableListOf()
    order.enforcementZoneConditions = mutableListOf()
    order.installationAndRisk = InstallationAndRisk(orderId = order.id)
    repo.save(order)
    return order
  }

  fun submitOrder(id: UUID, username: String): Order {
    val order = getOrder(username, id)!!

    if (order.status == OrderStatus.SUBMITTED) {
      throw SubmitOrderException("This order has already been submitted")
    }

    if (order.status == OrderStatus.ERROR) {
      throw SubmitOrderException("This order has encountered an error and cannot be submitted")
    }

    if (order.status == OrderStatus.IN_PROGRESS && !order.isValid) {
      throw SubmitOrderException("Please complete all mandatory fields before submitting this form")
    }

    if (order.status == OrderStatus.IN_PROGRESS && order.isValid) {
      try {
//    create FMS device wearer
        val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)
        val createDeviceWearerResult = fmsClient.createDeviceWearer(fmsDeviceWearer, orderId = id)
        order.fmsDeviceWearerId = createDeviceWearerResult.result.first().id

//    create FMS monitoring order
        val fmsOrder = MonitoringOrder.fromOrder(order)
        val createOrderResult = fmsClient.createMonitoringOrder(fmsOrder, id)
        order.fmsMonitoringOrderId = createOrderResult.result.first().id
//    TODO: Upload attachments
      } catch (e: Exception) {
        order.status = OrderStatus.ERROR
        repo.save(order)
        throw SubmitOrderException("The order could not be submitted to Serco", e)
      }
      order.status = OrderStatus.SUBMITTED
      repo.save(order)
    }

    return order
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
    ) ?: throw EntityNotFoundException("Order ($id) for $username not found")
  }
}
