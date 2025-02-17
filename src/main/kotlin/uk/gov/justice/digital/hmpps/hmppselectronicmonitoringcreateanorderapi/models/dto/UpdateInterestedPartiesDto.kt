package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto
import jakarta.validation.constraints.AssertTrue
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MagistrateCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation

data class UpdateInterestedPartiesDto(
  val notifyingOrganisation: NotifyingOrganisation,

  val notifyingOrganisationEmail: String = "",

  val notifyingOrganisationName: String = "",

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
) {
  @AssertTrue(message = "A valid Notifying Organisation Name is required")
  fun isNotifyingOrganisationName(): Boolean {
    if (notifyingOrganisation === NotifyingOrganisation.PRISON) {
      return Prison.entries.any { it.name === notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisation.CROWN_COURT) {
      return CrownCourt.entries.any { it.name === notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisation.MAGISTRATES_COURT) {
      return MagistrateCourt.entries.any { it.name === notifyingOrganisationName }
    }

    return notifyingOrganisationName === ""
  }
}
