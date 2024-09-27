package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ErrorMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerService
import java.time.LocalDate
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class DeviceWearerController(
  @Autowired val deviceWearerService: DeviceWearerService,
) {

  @ExceptionHandler(NoSuchElementException::class)
  fun handleEmptyResultException(e: NoSuchElementException): ResponseEntity<ErrorMessage> {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ErrorMessage(404, "Not Found"))
  }

  @GetMapping("/CreateDeviceWearer")
  fun createDeviceWearer(
    @RequestParam("orderId") orderId: UUID,
  ): ResponseEntity<DeviceWearer> {
    val deviceWearer = deviceWearerService.createDeviceWearer(orderId)
    return ResponseEntity(deviceWearer, HttpStatus.OK)
  }

  @GetMapping("/GetDeviceWearer")
  fun getDeviceWearer(
    @RequestParam("orderId")
    orderId: UUID,
    authentication: Authentication,
  ): ResponseEntity<DeviceWearer> {
    val username = authentication.name
    val deviceWearer = deviceWearerService.getDeviceWearer(username, orderId)
    return ResponseEntity(deviceWearer, HttpStatus.OK)
  }

  @PatchMapping("/UpdateDeviceWearer")
  fun updateDeviceWearer(
    @RequestParam("orderId") orderId: UUID,
    @RequestParam("firstName") firstName: String? = null,
    @RequestParam("lastName") lastName: String? = null,
    @RequestParam("alias") alias: String? = null,
    @RequestParam("gender") gender: String? = null,
    @RequestParam("dateOfBirth") dateOfBirth: LocalDate? = null,
    authentication: Authentication,
  ): ResponseEntity<DeviceWearer> {
    val username = authentication.name
    val deviceWearer = deviceWearerService.updateDeviceWearer(
      username,
      orderId,
      firstName,
      lastName,
      alias,
      gender,
      dateOfBirth,
    )
    return ResponseEntity(deviceWearer, HttpStatus.OK)
  }
}
