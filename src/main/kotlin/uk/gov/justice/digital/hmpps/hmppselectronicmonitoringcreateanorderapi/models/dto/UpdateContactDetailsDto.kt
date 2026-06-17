package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors

data class UpdateContactDetailsDto(
  @field:NotNull(message = ValidationErrors.ContactDetails.PHONE_NUMBER_REQUIRED)
  val phoneNumberAvailable: Boolean? = null,
  val contactNumber: String? = null,
)
