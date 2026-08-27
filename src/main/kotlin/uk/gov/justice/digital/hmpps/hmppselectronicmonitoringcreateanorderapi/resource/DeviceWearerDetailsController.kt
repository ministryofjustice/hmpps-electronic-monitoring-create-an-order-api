package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.GetCorePersonDetailsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.StoreDeviceWearerDetailsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerDetailsService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class DeviceWearerDetailsController(@Autowired val deviceWearerDetailsService: DeviceWearerDetailsService) {

  @GetMapping("/orders/{orderId}/device-wearer-details")
  fun getDeviceWearerDetails(
    @PathVariable orderId: UUID,
    @RequestParam organisationSearchId: String,
    authentication: Authentication,
  ): ResponseEntity<GetCorePersonDetailsResponse> {
    val username = authentication.name
    val details = deviceWearerDetailsService.getDetailsOverview(organisationSearchId, orderId, username)

    return ResponseEntity(details, HttpStatus.OK)
  }

  @PutMapping("/orders/{orderId}/device-wearer-details")
  fun storeDeviceWearerDetails(
    @PathVariable orderId: UUID,
    @RequestBody @Valid storeDeviceWearerDetailsDto: StoreDeviceWearerDetailsDto,
    authentication: Authentication,
  ): ResponseEntity<Unit> {
    val username = authentication.name
    deviceWearerDetailsService.storeDetails(storeDeviceWearerDetailsDto.organisationSearchId!!, orderId, username)

    return ResponseEntity(HttpStatus.NO_CONTENT)
  }
}
