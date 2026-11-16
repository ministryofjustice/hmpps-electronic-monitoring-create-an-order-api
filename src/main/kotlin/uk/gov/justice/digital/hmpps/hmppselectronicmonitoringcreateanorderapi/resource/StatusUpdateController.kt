package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.StatusUpdateDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.StatusUpdateService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class StatusUpdateController(private val statusUpdateService: StatusUpdateService) {

  @GetMapping("/orders/{orderId}/status-updates")
  fun getStatusUpdates(@PathVariable orderId: UUID): ResponseEntity<List<StatusUpdateDto>> =
    ResponseEntity(statusUpdateService.getStatusUpdates(orderId), HttpStatus.OK)
}
