package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType

data class UpdateAddressDto(
  val addressType: AddressType,
  val hasAnotherAddress: Boolean? = null,

  @field:NotBlank(message = ValidationErrors.Address.ADDRESS_1_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.Address.ADDRESS_1_MAX_LENGTH)
  val addressLine1: String = "",

  @field:Size(max = 200, message = ValidationErrors.Address.ADDRESS_2_MAX_LENGTH)
  val addressLine2: String = "",

  @field:NotBlank(message = ValidationErrors.Address.ADDRESS_3_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.Address.ADDRESS_3_MAX_LENGTH)
  val addressLine3: String = "",

  @field:Size(max = 200, message = ValidationErrors.Address.ADDRESS_4_MAX_LENGTH)
  val addressLine4: String = "",

  @field:NotBlank(message = ValidationErrors.Address.POSTCODE_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.Address.POSTCODE_MAX_LENGTH)
  val postcode: String = "",
)
