package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OrderFormService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class OrderFormController(
  @Autowired val orderFromService: OrderFormService,
) {

  @GetMapping("/CreateForm")
  fun createForm(authentication: Authentication): ResponseEntity<OrderForm> {
    val username = authentication.name

    val form = orderFromService.createOrderForm(username)
    return ResponseEntity(form, HttpStatus.OK)
  }

  @GetMapping("/GetForm")
  fun getForm(@RequestParam("id") id: UUID, authentication: Authentication): ResponseEntity<OrderForm> {
    val form = orderFromService.getOrderForm(id)
    return ResponseEntity(form, HttpStatus.OK)
  }
}
