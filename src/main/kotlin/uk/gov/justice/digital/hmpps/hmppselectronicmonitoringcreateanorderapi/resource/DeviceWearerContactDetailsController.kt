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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerContactDetailsService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class DeviceWearerContactDetailsController(
  @Autowired val contactDetailsService: DeviceWearerContactDetailsService,
) {

  @PutMapping("/orders/{orderId}/contact-details")
  fun updateContactDetails(
    @PathVariable orderId: UUID,
    @RequestBody @Valid contactDetailsUpdateRecord: UpdateContactDetailsDto,
    authentication: Authentication,
  ): ResponseEntity<DeviceWearerContactDetails> {
    val username = authentication.name
    val contactDetails = contactDetailsService.updateContactDetails(orderId, username, contactDetailsUpdateRecord)

    return ResponseEntity(contactDetails, HttpStatus.OK)
  }
}

data class UpdateContactDetailsDto(
  val contactNumber: String? = null,
)