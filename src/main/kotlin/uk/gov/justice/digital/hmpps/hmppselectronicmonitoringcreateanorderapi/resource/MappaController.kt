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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderParameters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateIsMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.MappaService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class MappaController(val service: MappaService) {
  @PutMapping("/orders/{orderId}/mappa")
  fun updateMappa(
    @PathVariable orderId: UUID,
    @RequestBody @Valid dto: UpdateMappaDto,
    authentication: Authentication,
  ): ResponseEntity<Mappa> {
    val username = authentication.name
    val mappa = service.updateMappa(orderId, username, dto)

    return ResponseEntity(mappa, HttpStatus.OK)
  }

  @PutMapping("/orders/{orderId}/mappa/is-mappa")
  fun isMappa(
    @PathVariable orderId: UUID,
    @RequestBody @Valid dto: UpdateIsMappaDto,
    authentication: Authentication,
  ): ResponseEntity<OrderParameters> {
    val username = authentication.name
    val parameters = service.updateIsMappa(
      orderId,
      username,
      dto,
    )

    return ResponseEntity(parameters, HttpStatus.OK)
  }
}
