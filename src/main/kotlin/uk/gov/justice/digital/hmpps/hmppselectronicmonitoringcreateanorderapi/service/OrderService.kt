package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SubmitOrderException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification.OrderSpecification
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.UUID

@Service
class OrderService(
  val repo: OrderRepository,
  val fmsService: FmsService,

) {
  fun createOrder(username: String, createRecord: CreateOrderDto): Order {
    val order = Order(
      username = username,
      status = OrderStatus.IN_PROGRESS,
      type = createRecord.type,
    )

    order.deviceWearer = DeviceWearer(orderId = order.id)
    order.addresses = mutableListOf()
    order.monitoringConditions = MonitoringConditions(orderId = order.id)
    order.additionalDocuments = mutableListOf()
    order.enforcementZoneConditions = mutableListOf()

    repo.save(order)
    return order
  }

  fun deleteOrder(id: UUID, username: String) {
    val order = repo.findByUsernameAndId(username, id).orElseThrow {
      EntityNotFoundException("An order with id $id does not exist")
    }

    if (order.status == OrderStatus.SUBMITTED) {
      throw IllegalStateException("Order with id $id cannot be deleted because it has already been submitted")
    }

    repo.delete(order)
  }

  fun getOrder(username: String, id: UUID): Order? {
    return repo.findByUsernameAndId(
      username,
      id,
    ).orElseThrow {
      EntityNotFoundException("Order ($id) for $username not found")
    }
  }

  fun submitOrder(id: UUID, username: String): Order {
    val order = getOrder(username, id)!!

    if (order.type == OrderType.VARIATION) {
      throw SubmitOrderException("A variation cannot be submitted yet!")
    }

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
        val submitResult = fmsService.submitOrder(order, FmsOrderSource.CEMO)
        order.fmsResultId = submitResult.id

        if (!submitResult.success) {
          order.status = OrderStatus.ERROR
          repo.save(order)
          throw SubmitOrderException("The order could not be submitted to Serco")
        } else {
          order.status = OrderStatus.SUBMITTED
          repo.save(order)
        }
      } catch (e: Exception) {
        order.status = OrderStatus.ERROR
        repo.save(order)
        throw SubmitOrderException("The order could not be submitted to Serco", e)
      }
    }
    return order
  }

  fun listOrders(searchCriteria: OrderSearchCriteria): List<Order> {
    return repo.findAll(
      OrderSpecification(searchCriteria),
    )
  }
}
