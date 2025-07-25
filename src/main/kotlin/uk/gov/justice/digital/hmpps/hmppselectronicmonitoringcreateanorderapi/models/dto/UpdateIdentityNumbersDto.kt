package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors

data class UpdateIdentityNumbersDto(
  @field:Size(max = 200, message = ValidationErrors.IdentityNumbers.NOMIS_ID_MAX_LENGTH)
  val nomisId: String? = "",

  @field:Size(max = 200, message = ValidationErrors.IdentityNumbers.PNC_ID_MAX_LENGTH)
  val pncId: String? = "",

  @field:Size(max = 200, message = ValidationErrors.IdentityNumbers.DELIUS_ID_MAX_LENGTH)
  val deliusId: String? = "",

  @field:Size(max = 200, message = ValidationErrors.IdentityNumbers.PRISON_NUMBER_MAX_LENGTH)
  val prisonNumber: String? = "",

  @field:Size(max = 200, message = ValidationErrors.IdentityNumbers.HOME_OFFICE_REFERENCE_NUMBER_MAX_LENGTH)
  var homeOfficeReferenceNumber: String? = "",
)
