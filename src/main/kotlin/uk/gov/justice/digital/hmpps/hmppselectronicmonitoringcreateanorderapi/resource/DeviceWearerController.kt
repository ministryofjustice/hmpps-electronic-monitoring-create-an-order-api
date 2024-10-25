package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerService
import java.time.ZonedDateTime
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class DeviceWearerController(
  @Autowired val deviceWearerService: DeviceWearerService,
) {

  @PutMapping("/orders/{orderId}/device-wearer")
  fun updateDeviceWearer(
    @PathVariable orderId: UUID,
    @RequestBody @Valid deviceWearerUpdateRecord: UpdateDeviceWearerDto,
    authentication: Authentication,
  ): ResponseEntity<DeviceWearer> {
    val username = authentication.name
    val deviceWearer = deviceWearerService.updateDeviceWearer(
      orderId,
      username,
      deviceWearerUpdateRecord,
    )

    return ResponseEntity(deviceWearer, HttpStatus.OK)
  }

  @PutMapping("/orders/{orderId}/device-wearer/no-fixed-abode")
  fun updateNoFixedAbode(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateNoFixedAbodeDto,
    authentication: Authentication,
  ): ResponseEntity<DeviceWearer> {
    val username = authentication.name
    val deviceWearer = deviceWearerService.updateNoFixedAbode(
      orderId,
      username,
      updateRecord,
    )

    return ResponseEntity(deviceWearer, HttpStatus.OK)
  }
}

data class UpdateDeviceWearerDto(
  val nomisId: String? = null,

  val pncId: String? = null,

  val deliusId: String? = null,

  val prisonNumber: String? = null,

  var homeOfficeReferenceNumber: String? = null,

  @field:Size(min = 1, message = "First name is required")
  val firstName: String? = null,

  @field:Size(min = 1, message = "Last name is required")
  val lastName: String? = null,

  val alias: String? = null,

  @field:NotNull(message = "You must indicate whether the device wearer will be an adult at installation")
  var adultAtTimeOfInstallation: Boolean? = null,

  @field:Size(min = 1, message = "Sex is required")
  var sex: String? = null,

  @field:Size(min = 1, message = "Gender is required")
  val gender: String? = null,

  @field:NotNull(message = "Date of birth is required")
  @field:Past(message = "Date of birth must be in the past")
  val dateOfBirth: ZonedDateTime? = null,

  val disabilities: String? = null,

  val language: String? = null,

  @field:NotNull(message = "You must indicate whether the device wearer will require an interpreter on the day of installation")
  val interpreterRequired: Boolean? = null,
) {

  @AssertTrue(message = "Device wearer's main language is required")
  fun isLanguage(): Boolean {
    if (this.interpreterRequired != null && this.interpreterRequired) {
      return this.language != null && this.language != ""
    }
    return true
  }
}

data class UpdateNoFixedAbodeDto(
  @field:NotNull(message = "You must indicate whether the device wearer has a fixed abode")
  var noFixedAbode: Boolean? = null,
)
