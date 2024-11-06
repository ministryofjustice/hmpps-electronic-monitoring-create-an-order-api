package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto
import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType

data class UpdateAddressDto(
  val addressType: AddressType,

  @field:NotBlank(message = "Address line 1 is required")
  val addressLine1: String = "",

  @field:NotBlank(message = "Address line 2 is required")
  val addressLine2: String = "",

  val addressLine3: String = "",

  val addressLine4: String = "",

  @field:NotBlank(message = "Postcode is required")
  val postcode: String = "",
)
