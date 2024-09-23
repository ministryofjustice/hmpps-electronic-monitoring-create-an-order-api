package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerService
import java.time.LocalDate
import java.util.UUID

@RestController
// TODO: Replace with CEMO Role one created
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@RequestMapping("/api/")
class DeviceWearerController(
  @Autowired val deviceWearerService: DeviceWearerService,
) {

  @GetMapping("/CreateDeviceWearer")
  fun createDeviceWearer(
    @RequestParam("orderId") orderId: UUID,
    @RequestParam("firstName") firstName: String? = null,
    @RequestParam("lastName") lastName: String? = null,
    @RequestParam("gender") gender: String? = null,
    @RequestParam("dateOfBirth") dateOfBirth: LocalDate? = null,
  ): ResponseEntity<DeviceWearer> {
    val deviceWearer = deviceWearerService.createDeviceWearer(orderId, firstName, lastName, gender, dateOfBirth)
    return ResponseEntity(deviceWearer, HttpStatus.OK)
  }
}
