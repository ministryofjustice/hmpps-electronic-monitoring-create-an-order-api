package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ServiceRequestType

class UpdateAmendOrderDto(
  @field:NotNull(message = ValidationErrors.AmendOrder.TYPE_REQUIRED)
  val type: ServiceRequestType? = null,
)
