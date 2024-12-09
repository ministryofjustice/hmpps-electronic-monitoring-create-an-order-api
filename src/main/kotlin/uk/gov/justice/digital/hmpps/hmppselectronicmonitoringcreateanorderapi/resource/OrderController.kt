package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OrderService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class OrderController(
  @Autowired val orderService: OrderService,
) {

  @PostMapping("/orders")
  fun createOrder(
    authentication: Authentication,
    @RequestBody @Valid createOrderRecord: CreateOrderDto = CreateOrderDto(),
  ): ResponseEntity<Order> {
    val username = authentication.name
    val order = orderService.createOrder(username, createOrderRecord)

    return ResponseEntity(order, HttpStatus.OK)
  }

  @PostMapping("/orders/{orderId}/submit")
  fun submitOrder(@PathVariable orderId: UUID, authentication: Authentication): ResponseEntity<Order> {
    val username = authentication.name
    val order = orderService.submitOrder(orderId, username)
    return ResponseEntity(order, HttpStatus.OK)
  }

  @GetMapping("/orders/{orderId}")
  fun getOrder(@PathVariable orderId: UUID, authentication: Authentication): ResponseEntity<Order> {
    val username = authentication.name
    val order = orderService.getOrder(username, orderId)

    return ResponseEntity(order, HttpStatus.OK)
  }

  @DeleteMapping("/orders/{orderId}")
  fun deleteOrder(@PathVariable orderId: UUID, authentication: Authentication): ResponseEntity<Void> {
    val username = authentication.name

    orderService.deleteOrder(orderId, username)

    return ResponseEntity(HttpStatus.NO_CONTENT)
  }

  @GetMapping("/orders")
  fun listOrders(@RequestParam searchTerm: String = "", authentication: Authentication): ResponseEntity<List<Order>> {
    val username = authentication.name
    val orders = orderService.listOrders(OrderSearchCriteria(searchTerm, username))

    return ResponseEntity(orders, HttpStatus.OK)
  }
}
