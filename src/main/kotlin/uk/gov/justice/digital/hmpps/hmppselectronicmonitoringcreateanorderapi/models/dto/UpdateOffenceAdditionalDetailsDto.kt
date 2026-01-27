package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.util.UUID

data class UpdateOffenceAdditionalDetailsDto(
  val id: UUID? = null,

  @field:Size(max = 200, message = ValidationErrors.OffenceAdditionalDetails.OFFENCE_DETAILS_TOO_LONG)
  val additionalDetails: String? = null,
)
