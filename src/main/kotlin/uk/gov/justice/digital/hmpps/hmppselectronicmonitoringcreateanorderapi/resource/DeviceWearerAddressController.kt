package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
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

  @PutMapping("/orders/{orderId}/address")
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
  val noFixedAbode: Boolean = false,
  val installationAddress: Boolean = false,
  val addressLine1: String,
  val addressLine2: String,
  val addressLine3: String,
  val addressLine4: String,
  val postcode: String,
) {
  @AssertTrue(message = "noFixedAbode can only be true for a primary address")
  fun isNoFixedAbode(): Boolean {
    if (this.addressType === DeviceWearerAddressType.PRIMARY) {
      return true
    }
    return !this.noFixedAbode
  }

  @AssertTrue(message = "installationAddress can only be true for a primary address")
  fun isInstallationAddress(): Boolean {
    if (this.addressType === DeviceWearerAddressType.PRIMARY) {
      if (this.noFixedAbode) {
        return !this.installationAddress
      }
      return true
    }
    return !this.installationAddress
  }

  @AssertTrue(message = "Address line 1 is required")
  fun isAddressLine1(): Boolean {
    if (this.noFixedAbode) {
      return true
    }

    return this.addressLine1.isNotBlank()
  }

  @AssertTrue(message = "Address line 2 is required")
  fun isAddressLine2(): Boolean {
    if (this.noFixedAbode) {
      return true
    }

    return this.addressLine2.isNotBlank()
  }

  @AssertTrue(message = "Postcode is required")
  fun isPostcode(): Boolean {
    if (this.noFixedAbode) {
      return true
    }

    return this.postcode.isNotBlank()
  }
}
