package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Dapo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDapoDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DapoService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class DapoController(val service: DapoService) {

  @PutMapping("/orders/{orderId}/dapo")
  fun updateDapo(
    @PathVariable orderId: UUID,
    @RequestBody @Valid dto: UpdateDapoDto,
    authentication: Authentication,
  ): ResponseEntity<Dapo> {
    val username = authentication.name
    val dapo = service.updateDapo(
      orderId,
      username,
      dto,
    )

    return ResponseEntity(dapo, HttpStatus.OK)
  }
}
