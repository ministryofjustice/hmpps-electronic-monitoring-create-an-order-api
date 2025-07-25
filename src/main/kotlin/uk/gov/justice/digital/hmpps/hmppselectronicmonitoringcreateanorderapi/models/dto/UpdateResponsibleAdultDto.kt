package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidPhoneNumber

data class UpdateResponsibleAdultDto(
  @field:NotBlank(message = ValidationErrors.ResponsibleAdult.FULL_NAME_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.ResponsibleAdult.FULL_NAME_MAX_LENGTH)
  val fullName: String,

  @field:NotBlank(message = ValidationErrors.ResponsibleAdult.RELATIONSHIP_REQUIRED)
  val relationship: String,

  val otherRelationshipDetails: String?,

  @field:ValidPhoneNumber
  val contactNumber: String? = null,
) {
  @AssertTrue(message = ValidationErrors.ResponsibleAdult.RELATIONSHIP_DETAILS_REQUIRED)
  fun isOtherRelationshipDetails(): Boolean = !(relationship == "other" && otherRelationshipDetails.isNullOrBlank())
}
