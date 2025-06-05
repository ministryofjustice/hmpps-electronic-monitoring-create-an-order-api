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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateProbationDeliveryUnitDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.ProbationDeliveryUnitService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class ProbationDeliveryUnitController(@Autowired val probationDeliveryUnitService: ProbationDeliveryUnitService) {

  @PutMapping("/orders/{orderId}/probation-delivery-unit")
  fun updateContactDetails(
    @PathVariable orderId: UUID,
    @RequestBody @Valid probationDeliveryUnitUpdateRecord: UpdateProbationDeliveryUnitDto,
    authentication: Authentication,
  ): ResponseEntity<ProbationDeliveryUnit> {
    val username = authentication.name
    val probationDeliveryUnit = probationDeliveryUnitService.updateProbationDeliveryUnit(
      orderId,
      username,
      probationDeliveryUnitUpdateRecord,
    )

    return ResponseEntity(probationDeliveryUnit, HttpStatus.OK)
  }
}
