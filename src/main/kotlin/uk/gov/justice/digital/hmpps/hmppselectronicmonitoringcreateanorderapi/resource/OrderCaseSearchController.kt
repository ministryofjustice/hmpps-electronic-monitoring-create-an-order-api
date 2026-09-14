package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderCaseSearchResultDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.FindOrderByCaseIdService

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__EXTERNAL_SEARCH')")
@RequestMapping("/api/orders/search")
class OrderCaseSearchController(private val findOrderByCaseIdService: FindOrderByCaseIdService) {

  @GetMapping("/by-case-id/{caseId}")
  fun findByCaseId(@PathVariable caseId: String): ResponseEntity<OrderCaseSearchResultDto> {
    val result = findOrderByCaseIdService.execute(caseId)
      ?: return ResponseEntity(HttpStatus.NOT_FOUND)

    return ResponseEntity(result, HttpStatus.OK)
  }
}
