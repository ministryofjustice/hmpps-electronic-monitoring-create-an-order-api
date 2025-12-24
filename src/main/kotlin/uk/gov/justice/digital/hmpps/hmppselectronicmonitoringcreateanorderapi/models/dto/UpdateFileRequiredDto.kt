package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType

class UpdateFileRequiredDto(

  val fileType: DocumentType? = null,
  @field:NotNull(message = ValidationErrors.AdditionalDocuments.HAVE_FIEL_REQIORED)
  val fileRequired: Boolean? = null,
)
