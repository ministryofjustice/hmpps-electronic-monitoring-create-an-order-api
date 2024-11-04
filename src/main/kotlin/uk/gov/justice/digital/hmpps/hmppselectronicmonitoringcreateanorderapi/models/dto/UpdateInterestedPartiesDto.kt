package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto
import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation

data class UpdateInterestedPartiesDto(
  val notifyingOrganisationEmail: String = "",

  val responsibleOfficerName: String = "",

  val responsibleOfficerPhoneNumber: String? = null,

  val responsibleOrganisation: ResponsibleOrganisation? = null,

  val responsibleOrganisationRegion: String = "",

  val responsibleOrganisationPhoneNumber: String? = null,

  val responsibleOrganisationEmail: String = "",

  @field:NotBlank(message = "Address line 1 is required")
  val responsibleOrganisationAddressLine1: String = "",

  @field:NotBlank(message = "Address line 2 is required")
  val responsibleOrganisationAddressLine2: String = "",

  val responsibleOrganisationAddressLine3: String = "",

  val responsibleOrganisationAddressLine4: String = "",

  @field:NotBlank(message = "Postcode is required")
  val responsibleOrganisationPostcode: String = "",
)
