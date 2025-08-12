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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationAppointmentDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.InstallationAppointmentService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class InstallationAppointmentController(@Autowired val installationAppointmentService: InstallationAppointmentService) {

  @PutMapping("/orders/{orderId}/installation-appointment")
  fun updateInstallationLocation(
    @PathVariable orderId: UUID,
    @RequestBody @Valid installationAppointmentDto: UpdateInstallationAppointmentDto,
    authentication: Authentication,
  ): ResponseEntity<InstallationAppointment> {
    val username = authentication.name
    val probationDeliveryUnit = installationAppointmentService.createOrUpdateInstallationAppointment(
      orderId,
      username,
      installationAppointmentDto,
    )

    return ResponseEntity(probationDeliveryUnit, HttpStatus.OK)
  }
}
