package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateResponsibleAdultDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.ResponsibleAdultService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class ResponsibleAdultController(
  @Autowired val responsibleAdultService: ResponsibleAdultService,
) {
  @PutMapping("/orders/{orderId}/device-wearer-responsible-adult")
  fun updateResponsibleAdult(
    @PathVariable orderId: UUID,
    @RequestBody @Valid responsibleAdultUpdateRecord: UpdateResponsibleAdultDto,
    authentication: Authentication,
  ): ResponseEntity<ResponsibleAdult> {
    val username = authentication.name
    val responsibleAdult = responsibleAdultService.updateResponsibleAdult(
      orderId,
      username,
      responsibleAdultUpdateRecord,
    )

    return ResponseEntity(responsibleAdult, HttpStatus.OK)
  }
}
