package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
  @Operation(
    summary = "Create a new order",
    description = "Creates a new electronic monitoring order",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully created order",
        content = [
          Content(
            mediaType = "application/json",
            schema = io.swagger.v3.oas.annotations.media.Schema(implementation = Order::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden - user not authorised to create an electronic monitoring order",
      ),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  fun createOrder(
    authentication: Authentication,
    @RequestBody @Valid createOrderRecord: CreateOrderDto = CreateOrderDto(),
  ): ResponseEntity<Order> {
    val username = authentication.name
    val order = orderService.createOrder(username, createOrderRecord)

    return ResponseEntity(order, HttpStatus.OK)
  }

  @PostMapping("/orders/{orderId}/submit")
  @Operation(
    summary = "Submit an order",
    description = "Submits an electronic monitoring order for action",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully submitted the order",
        content = [
          Content(mediaType = "application/json", schema = Schema(implementation = Order::class)),
        ],
      ),
      ApiResponse(responseCode = "400", description = "Order is in an invalid state and cannot be submitted"),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden - user not authorised to submit electronic monitoring order",
      ),
      ApiResponse(responseCode = "404", description = "Order not found"),
    ],
  )
  fun submitOrder(@PathVariable orderId: UUID, authentication: Authentication): ResponseEntity<Order> {
    val username = authentication.name
    val order = orderService.submitOrder(orderId, username)
    return ResponseEntity(order, HttpStatus.OK)
  }

  @GetMapping("/orders/{orderId}")
  @Operation(
    summary = "Get order",
    description = "Fetches all information associated with an order, using the order ID",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved the order",
        content = [
          Content(mediaType = "application/json", schema = Schema(implementation = Order::class)),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden - user not authorised to get electronic monitoring order",
      ),
      ApiResponse(responseCode = "404", description = "Order not found"),
    ],
  )
  fun getOrder(@PathVariable orderId: UUID, authentication: Authentication): ResponseEntity<Order> {
    val username = authentication.name
    val order = orderService.getOrder(username, orderId)

    return ResponseEntity(order, HttpStatus.OK)
  }

  @DeleteMapping("/orders/{orderId}")
  @Operation(
    summary = "Delete an order by ID",
    description = "Deletes an in-progress electronic monitoring order from the system based on the provided order ID.",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully deleted the order",
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden - user not authorised to delete electronic monitoring order",
      ),
      ApiResponse(responseCode = "404", description = "Order not found"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  fun deleteOrder(@PathVariable orderId: UUID, authentication: Authentication): ResponseEntity<Void> {
    val username = authentication.name

    orderService.deleteOrder(orderId, username)

    return ResponseEntity(HttpStatus.NO_CONTENT)
  }

  @GetMapping("/orders")
  @Operation(
    summary = "Get a list of all orders",
    description = "Retrieves a list of all the electronic orders in the system.",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved list of orders",
        content = [
          Content(mediaType = "application/json", schema = Schema(implementation = List::class)),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden - user not authorised to get an electronic monitoring orders",
      ),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  fun listOrders(@RequestParam searchTerm: String = "", authentication: Authentication): ResponseEntity<List<Order>> {
    val username = authentication.name
    val orders = orderService.listOrders(OrderSearchCriteria(searchTerm, username))

    return ResponseEntity(orders, HttpStatus.OK)
  }
}
