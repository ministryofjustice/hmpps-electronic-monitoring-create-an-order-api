package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation

data class UpdateInterestedPartiesDto(
  val notifyingOrganisationEmail: String = "",

  val responsibleOfficerName: String = "",

  val responsibleOfficerPhoneNumber: String? = null,

  val responsibleOrganisation: ResponsibleOrganisation? = null,

  val responsibleOrganisationRegion: String = "",

  val responsibleOrganisationPhoneNumber: String? = null,

  val responsibleOrganisationEmail: String = "",

  val responsibleOrganisationAddressLine1: String = "",

  val responsibleOrganisationAddressLine2: String = "",

  val responsibleOrganisationAddressLine3: String = "",

  val responsibleOrganisationAddressLine4: String = "",

  val responsibleOrganisationAddressPostcode: String = "",
)
