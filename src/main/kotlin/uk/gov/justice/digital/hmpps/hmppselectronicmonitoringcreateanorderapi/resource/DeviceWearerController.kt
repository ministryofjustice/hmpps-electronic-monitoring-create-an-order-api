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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDeviceWearerDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateIdentityNumbersDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateNoFixedAbodeDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerService
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

  @PutMapping("/orders/{orderId}/device-wearer/identity-numbers")
  fun updateIdentityNumbers(
    @PathVariable orderId: UUID,
    @RequestBody @Valid updateRecord: UpdateIdentityNumbersDto,
    authentication: Authentication,
  ): ResponseEntity<DeviceWearer> {
    val username = authentication.name
    val deviceWearer = deviceWearerService.updateIdentityNumbers(
      orderId,
      username,
      updateRecord,
    )

    return ResponseEntity(deviceWearer, HttpStatus.OK)
  }
}
