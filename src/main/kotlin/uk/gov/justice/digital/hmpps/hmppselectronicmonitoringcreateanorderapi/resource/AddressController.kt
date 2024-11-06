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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateAddressDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.AddressService
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class AddressController(
  @Autowired val addressService: AddressService,
) {
  @PutMapping("/orders/{orderId}/address")
  fun updateAddress(
    @PathVariable orderId: UUID,
    @RequestBody @Valid addressUpdateRecord: UpdateAddressDto,
    authentication: Authentication,
  ): ResponseEntity<Address> {
    val username = authentication.name
    val address = addressService.updateAddress(
      orderId,
      username,
      addressUpdateRecord,
    )

    return ResponseEntity(address, HttpStatus.OK)
  }
}
