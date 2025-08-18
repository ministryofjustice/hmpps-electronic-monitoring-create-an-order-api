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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OrderService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class OrderController(@Autowired val orderService: OrderService) {

  @PostMapping("/orders")
  fun createOrder(
    authentication: Authentication,
    @RequestBody @Valid createOrderRecord: CreateOrderDto = CreateOrderDto(),
  ): ResponseEntity<OrderDto> {
    val username = authentication.name
    val order = orderService.createOrder(username, createOrderRecord)

    return ResponseEntity(convertToDto(order), HttpStatus.OK)
  }

  @PostMapping("/orders/{orderId}/submit")
  fun submitOrder(@PathVariable orderId: UUID, authentication: AuthAwareAuthenticationToken): ResponseEntity<OrderDto> {
    val username = authentication.name
    val userFullName = authentication.getUserFullName()
    val order = orderService.submitOrder(orderId, username, userFullName)
    return ResponseEntity(convertToDto(order), HttpStatus.OK)
  }

  @PostMapping("/orders/{orderId}/copy-as-variation")
  fun createVariationFromExisting(
    @PathVariable orderId: UUID,
    authentication: Authentication,
  ): ResponseEntity<OrderDto> {
    val username = authentication.name
    val newVariation = orderService.createVariationFromExisting(orderId, username)
    return ResponseEntity(convertToDto(newVariation), HttpStatus.OK)
  }

  @GetMapping("/orders/{orderId}")
  fun getOrder(@PathVariable orderId: UUID, authentication: Authentication): ResponseEntity<OrderDto> {
    val username = authentication.name
    val order = orderService.getOrder(orderId, username)

    return ResponseEntity(convertToDto(order), HttpStatus.OK)
  }

  @DeleteMapping("/orders/{orderId}")
  fun deleteOrder(@PathVariable orderId: UUID, authentication: Authentication): ResponseEntity<Void> {
    val username = authentication.name

    orderService.deleteCurrentVersionForOrder(orderId, username)

    return ResponseEntity(HttpStatus.NO_CONTENT)
  }

  @GetMapping("/orders")
  fun listOrders(
    @RequestParam searchTerm: String = "",
    authentication: Authentication,
  ): ResponseEntity<List<OrderDto>> {
    val username = authentication.name
    val orders = orderService.listOrders(OrderListCriteria(searchTerm, username))

    return ResponseEntity(orders.map { convertToDto(it) }, HttpStatus.OK)
  }

  @GetMapping("/orders/search")
  fun searchOrders(
    @RequestParam searchTerm: String = "",
    authentication: Authentication,
  ): ResponseEntity<List<OrderDto>> {
    val orders = orderService.searchOrders(OrderSearchCriteria(searchTerm))

    return ResponseEntity(orders.map { convertToDto(it) }, HttpStatus.OK)
  }

  private fun convertToDto(order: Order): OrderDto = OrderDto(
    id = order.id,
    additionalDocuments = order.additionalDocuments,
    addresses = order.addresses,
    contactDetails = order.contactDetails,
    curfewConditions = order.curfewConditions,
    curfewReleaseDateConditions = order.curfewReleaseDateConditions,
    curfewTimeTable = order.curfewTimeTable,
    deviceWearer = order.deviceWearer,
    deviceWearerResponsibleAdult = order.deviceWearerResponsibleAdult,
    enforcementZoneConditions = order.enforcementZoneConditions,
    fmsResultId = order.fmsResultId,
    fmsResultDate = order.fmsResultDate,
    installationAndRisk = order.installationAndRisk,
    interestedParties = order.interestedParties,
    isValid = order.isValid,
    mandatoryAttendanceConditions = order.mandatoryAttendanceConditions,
    monitoringConditions = order.monitoringConditions,
    monitoringConditionsAlcohol = order.monitoringConditionsAlcohol,
    monitoringConditionsTrail = order.monitoringConditionsTrail,
    status = order.status,
    type = order.type,
    username = order.username,
    variationDetails = order.variationDetails,
    probationDeliveryUnit = order.probationDeliveryUnit,
    installationLocation = order.installationLocation,
    installationAppointment = order.installationAppointment,
    dataDictionaryVersion = order.dataDictionaryVersion,
    orderParameters = order.orderParameters,
    submittedBy = order.submittedBy,
  )

private fun convertToDto(order: OrderVersion): OrderDto = OrderDto(
    id = order.id,
    additionalDocuments = order.additionalDocuments,
    addresses = order.addresses,
    contactDetails = order.contactDetails,
    curfewConditions = order.curfewConditions,
    curfewReleaseDateConditions = order.curfewReleaseDateConditions,
    curfewTimeTable = order.curfewTimeTable,
    deviceWearer = order.deviceWearer,
    deviceWearerResponsibleAdult = order.deviceWearerResponsibleAdult,
    enforcementZoneConditions = order.enforcementZoneConditions,
    fmsResultId = order.fmsResultId,
    fmsResultDate = order.fmsResultDate,
    installationAndRisk = order.installationAndRisk,
    interestedParties = order.interestedParties,
    isValid = order.isValid,
    mandatoryAttendanceConditions = order.mandatoryAttendanceConditions,
    monitoringConditions = order.monitoringConditions,
    monitoringConditionsAlcohol = order.monitoringConditionsAlcohol,
    monitoringConditionsTrail = order.monitoringConditionsTrail,
    status = order.status,
    type = order.type,
    username = order.username,
    variationDetails = order.variationDetails,
    probationDeliveryUnit = order.probationDeliveryUnit,
    installationLocation = order.installationLocation,
    installationAppointment = order.installationAppointment,
    dataDictionaryVersion = order.dataDictionaryVersion,
    orderParameters = order.orderParameters,
    submittedBy = order.submittedBy,
  )
}
