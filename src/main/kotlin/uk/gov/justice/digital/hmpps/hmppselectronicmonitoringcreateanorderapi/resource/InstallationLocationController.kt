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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationLocationDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.InstallationLocationService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class InstallationLocationController(@Autowired val installationLocationService: InstallationLocationService) {

  @PutMapping("/orders/{orderId}/installation-location")
  fun updateInstallationLocation(
    @PathVariable orderId: UUID,
    @RequestBody @Valid installationLocationDto: UpdateInstallationLocationDto,
    authentication: Authentication,
  ): ResponseEntity<InstallationLocation> {
    val username = authentication.name
    val probationDeliveryUnit = installationLocationService.createOrUpdateInstallationLocation(
      orderId,
      username,
      installationLocationDto,
    )

    return ResponseEntity(probationDeliveryUnit, HttpStatus.OK)
  }
}
