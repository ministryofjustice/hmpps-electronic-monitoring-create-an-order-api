package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors

data class UpdateIsSentencingAct(
  @field:NotNull(message = ValidationErrors.IsSentencingAct.IS_SENTENCING_ACT_REQUIRED)
  var isSentencingAct: Boolean? = false,
)
