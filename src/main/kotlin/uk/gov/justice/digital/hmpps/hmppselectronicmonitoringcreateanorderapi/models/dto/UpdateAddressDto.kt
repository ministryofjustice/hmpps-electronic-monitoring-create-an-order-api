package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto
import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType

data class UpdateAddressDto(
  val addressType: AddressType,

  @field:NotBlank(message = ValidationErrors.Address.ADDRESS_1_REQUIRED)
  val addressLine1: String = "",

  val addressLine2: String = "",

  @field:NotBlank(message = ValidationErrors.Address.ADDRESS_3_REQUIRED)
  val addressLine3: String = "",

  val addressLine4: String = "",

  @field:NotBlank(message = ValidationErrors.Address.POSTCODE_REQUIRED)
  val postcode: String = "",
)
