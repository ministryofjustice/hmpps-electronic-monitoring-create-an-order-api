package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ErrorMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerContactDetailsService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class DeviceWearerContactDetailsController(
  @Autowired val contactDetailsService: DeviceWearerContactDetailsService,
) {

  @ExceptionHandler(EntityNotFoundException::class)
  fun handleEmptyResultException(e: EntityNotFoundException): ResponseEntity<ErrorMessage> {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ErrorMessage(404, "Not Found"))
  }

  @ExceptionHandler(ConstraintViolationException::class)
  fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<List<ValidationError>> {
    val  details: List<ValidationError> = e.constraintViolations.stream().map{ violation ->
      ValidationError(violation.propertyPath.toString(), violation.message )
    }.toList()

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(details)
  }

  @GetMapping("/order/{orderId}/contact-details")
  fun getContactDetails(
    @PathVariable orderId: UUID,
    authentication: Authentication,
  ): ResponseEntity<DeviceWearerContactDetails> {
    val username = authentication.name
    val contactDetails = contactDetailsService.getContactDetails(orderId, username)

    return ResponseEntity(contactDetails, HttpStatus.OK)
  }

  @PostMapping("/order/{orderId}/contact-details")
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
  val contactNumber: String,
)
