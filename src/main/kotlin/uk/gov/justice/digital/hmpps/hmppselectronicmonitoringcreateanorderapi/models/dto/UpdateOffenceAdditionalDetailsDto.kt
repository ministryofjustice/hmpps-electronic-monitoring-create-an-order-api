package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.util.UUID

data class UpdateOffenceAdditionalDetailsDto(
  val id: UUID? = null,

  @field:NotNull(message = ValidationErrors.OffenceAdditionalDetails.RESPONSE_REQUIRED)
  val additionalDetailsRequired: Boolean? = null,

  @field:Size(max = 200, message = ValidationErrors.OffenceAdditionalDetails.OFFENCE_DETAILS_TOO_LONG)
  val additionalDetails: String? = null,
) {

  @AssertTrue(message = ValidationErrors.OffenceAdditionalDetails.DETAILS_REQUIRED)
  fun isAdditionalDetails(): Boolean {
    if (this.additionalDetailsRequired == true) {
      return !this.additionalDetails.isNullOrBlank()
    }
    return true
  }
}
