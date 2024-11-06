package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SubmitOrderException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
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
      // try {

      val submitResult = fmsService.submitOrder(order, FmsOrderSource.CEMO)
      order.fmsResultId = submitResult.id

      if (!submitResult.success) {
        order.status = OrderStatus.ERROR
        repo.save(order)

        val error = ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = submitResult.error,
          developerMessage = submitResult.error,
        )
        throw SubmitOrderException("The order could not be submitted to Serco", Throwable(error.userMessage))
      } else {
        order.status = OrderStatus.SUBMITTED
        repo.save(order)
      }
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
