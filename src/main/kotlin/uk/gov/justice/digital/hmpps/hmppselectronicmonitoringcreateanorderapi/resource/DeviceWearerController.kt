package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
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

  @GetMapping("/order/{orderId}/device-wearer")
  fun getDeviceWearer(
    @PathVariable orderId: UUID,
    authentication: Authentication,
  ): ResponseEntity<DeviceWearer> {
    val username = authentication.name
    val deviceWearer = deviceWearerService.getDeviceWearer(
      orderId,
      username,
    )

    return ResponseEntity(deviceWearer, HttpStatus.OK)
  }

  @PostMapping("/order/{orderId}/device-wearer")
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
}

data class UpdateDeviceWearerDto(
  val firstName: String? = null,
  val lastName: String? = null,
  val alias: String? = null,
  val gender: String? = null,
  val dateOfBirth: ZonedDateTime? = null,
)
