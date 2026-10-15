package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AmendmentAction

class CreateAmendmentDto(
  @field:NotNull(message = "Select how you are making changes to the form")
  val action: AmendmentAction? = null,
)
