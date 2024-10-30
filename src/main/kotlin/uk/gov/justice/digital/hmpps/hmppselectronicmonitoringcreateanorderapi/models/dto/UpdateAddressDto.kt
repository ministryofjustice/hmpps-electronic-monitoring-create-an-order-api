package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType

data class UpdateAddressDto(
  val addressType: AddressType,
  val addressLine1: String = "",
  val addressLine2: String = "",
  val addressLine3: String = "",
  val addressLine4: String = "",
  val postcode: String = "",
)
