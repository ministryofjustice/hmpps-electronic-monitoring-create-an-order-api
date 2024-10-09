package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerAddressService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class DeviceWearerAddressController(
  @Autowired val deviceWearerAddressService: DeviceWearerAddressService,
) {

  @PostMapping("/order/{orderId}/address")
  fun updateContactDetails(
    @PathVariable orderId: UUID,
    @RequestBody @Valid deviceWearerAddressUpdateRecord: UpdateDeviceWearerAddressDto,
    authentication: Authentication,
  ): ResponseEntity<DeviceWearerAddress> {
    val username = authentication.name
    val address = deviceWearerAddressService.createOrUpdateAddress(
      orderId,
      username,
      deviceWearerAddressUpdateRecord,
    )

    return ResponseEntity(address, HttpStatus.OK)
  }
}

data class UpdateDeviceWearerAddressDto(
  val addressType: DeviceWearerAddressType,
  val addressLine1: String,
  val addressLine2: String,
  val addressLine3: String,
  val addressLine4: String,
  val postCode: String,
) {
  @AssertTrue(message = "Address line 1 is required")
  fun isAddressLine1(): Boolean {
    if (this.addressType === DeviceWearerAddressType.PRIMARY) {
      return this.addressLine1.isNotBlank()
    }
    return true
  }

  @AssertTrue(message = "Address line 2 is required")
  fun isAddressLine2(): Boolean {
    if (this.addressType === DeviceWearerAddressType.PRIMARY) {
      return this.addressLine2.isNotBlank()
    }
    return true
  }

  @AssertTrue(message = "Post code is required")
  fun isPostCode(): Boolean {
    if (this.addressType === DeviceWearerAddressType.PRIMARY) {
      return this.postCode.isNotBlank()
    }
    return true
  }
}
