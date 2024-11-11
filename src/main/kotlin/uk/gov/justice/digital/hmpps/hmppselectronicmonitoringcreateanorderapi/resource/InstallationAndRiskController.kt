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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.InstallationAndRiskService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class InstallationAndRiskController(
  @Autowired val service: InstallationAndRiskService,
) {

  @PutMapping("/orders/{orderId}/installation-and-risk")
  fun updateContactDetails(
    @PathVariable orderId: UUID,
    @RequestBody @Valid installationAndRisk: InstallationAndRisk,
    authentication: Authentication,
  ): ResponseEntity<InstallationAndRisk> {
    val username = authentication.name
    val risk = service.updateInstallationAndRisk(
      orderId,
      username,
      installationAndRisk,
    )

    return ResponseEntity(risk, HttpStatus.OK)
  }
}
