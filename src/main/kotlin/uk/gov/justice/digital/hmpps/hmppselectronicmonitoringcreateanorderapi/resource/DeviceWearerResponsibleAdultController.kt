package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidPhoneNumber
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerResponsibleAdultService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class DeviceWearerResponsibleAdultController(
  @Autowired val deviceWearerResponsibleAdultService: DeviceWearerResponsibleAdultService,
) {
  @PutMapping("/orders/{orderId}/device-wearer-responsible-adult")
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
  @field:NotBlank(message = "Full name is required")
  val fullName: String,
  @field:NotBlank(message = "Relationship is required")
  val relationship: String,
  val otherRelationshipDetails: String?,
  @field:ValidPhoneNumber
  val contactNumber: String,
) {
  @AssertTrue(message = "You must provide details of the responsible adult to the device wearer")
  fun isOtherRelationshipDetails(): Boolean {
    return !(relationship == "other" && otherRelationshipDetails.isNullOrBlank())
  }
}
