package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OrderFormService

@RestController
// TODO: Replace with CEMO Role one created
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class OrderFormController(
  @Autowired val orderFromService: OrderFormService,
) {

  @GetMapping("/CreateForm")
  fun createForm(@RequestParam("title") title: String, authentication: Authentication): ResponseEntity<OrderForm> {
    val username = authentication.name

    val form = orderFromService.createOrderForm(title, username)
    return ResponseEntity(form, HttpStatus.OK)
  }

  @PostMapping("/SubmitForm")
  fun submitForm(): ResponseEntity<SubmissionResult> {
    val result = orderFromService.submitOrderForm()
    return ResponseEntity(result, HttpStatus.OK)
  }
}
