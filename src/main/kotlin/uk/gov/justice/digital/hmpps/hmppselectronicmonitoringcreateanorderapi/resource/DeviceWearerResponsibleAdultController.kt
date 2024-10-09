package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerResponsibleAdultService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class DeviceWearerResponsibleAdultController(
  @Autowired val deviceWearerResponsibleAdultService: DeviceWearerResponsibleAdultService,
) {

  @GetMapping("/order/{orderId}/device-wearer-responsible-adult")
  fun getResponsibleAdult(
    @PathVariable orderId: UUID,
    authentication: Authentication,
  ): ResponseEntity<ResponsibleAdult> {
    val username = authentication.name
    val responsibleAdult = deviceWearerResponsibleAdultService.getResponsibleAdult(orderId, username)

    return ResponseEntity(responsibleAdult, HttpStatus.OK)
  }

  @PostMapping("/order/{orderId}/device-wearer-responsible-adult")
  fun updateResponsibleAdult(
    @PathVariable orderId: UUID,
    @RequestBody @Valid responsibleAdultUpdateRecord: UpdateDeviceWearerResponsibleAdultDto,
    authentication: Authentication,
  ): ResponseEntity<ResponsibleAdult> {
    val username = authentication.name
    val responsibleAdult = deviceWearerResponsibleAdultService.createOrUpdateResponsibleAdult(orderId, username, responsibleAdultUpdateRecord)

    return ResponseEntity(responsibleAdult, HttpStatus.OK)
  }
}

data class UpdateDeviceWearerResponsibleAdultDto(
  val fullName: String,
  val relationship: String,
  val otherRelationshipDetails: String?,
  val contactNumber: String,
) {
  @AssertTrue(message = "Full name is required")
  fun isFullName(): Boolean {
    return this.fullName.isNotBlank()
  }

  @AssertTrue(message = "Relationship is required")
  fun isRelationship(): Boolean {
    return this.relationship.isNotBlank()
  }

  @AssertTrue(message = "You must provide details of the responsible adult to the device wearer")
  fun isOtherRelationshipDetails(): Boolean {
    return !(relationship == "other" && otherRelationshipDetails.isNullOrBlank())
  }

  @AssertTrue(message = "Contact number is required")
  fun isContactNumber(): Boolean {
    return this.contactNumber.isNotBlank()
  }
}
