package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors

class UpdateIsMappaDto(
  @field:NotNull(message = ValidationErrors.Mappa.IS_MAPPA_REQUIRED)
  val isMappa: Boolean? = null,
)
