package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors

class UpdateHavePhotoDto(
  @field:NotNull(message = ValidationErrors.AdditionalDocuments.HAVE_PHOTO_REQUIRED)
  val havePhoto: Boolean,
)
