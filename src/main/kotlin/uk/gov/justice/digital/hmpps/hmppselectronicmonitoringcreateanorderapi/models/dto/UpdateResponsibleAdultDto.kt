package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidPhoneNumber

data class UpdateResponsibleAdultDto(
  @field:NotBlank(message = "Full name is required")
  val fullName: String,

  @field:NotBlank(message = "Relationship is required")
  val relationship: String,

  val otherRelationshipDetails: String?,

  @field:ValidPhoneNumber
  val contactNumber: String? = null,
) {
  @AssertTrue(message = "You must provide details of the responsible adult to the device wearer")
  fun isOtherRelationshipDetails(): Boolean {
    return !(relationship == "other" && otherRelationshipDetails.isNullOrBlank())
  }
}
