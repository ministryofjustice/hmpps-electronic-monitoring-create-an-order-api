package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MagistrateCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthJusticeServiceRegions

data class UpdateInterestedPartiesDto(
  @field:NotNull(message = ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_REQUIRED)
  val notifyingOrganisation: NotifyingOrganisation? = null,

  @field:NotNull(message = ValidationErrors.InterestedParties.TEAM_EMAIL_REQUIRED)
  val notifyingOrganisationEmail: String = "",

  val notifyingOrganisationName: String = "",

  @field:NotEmpty(message = ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_FULL_NAME_REQUIRED)
  val responsibleOfficerName: String = "",

  @field:NotNull(message = ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_TELEPHONE_NUMBER_REQUIRED)
  val responsibleOfficerPhoneNumber: String? = null,

  @field:NotNull(message = ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_REQUIRED)
  val responsibleOrganisation: ResponsibleOrganisation? = null,

  val responsibleOrganisationRegion: String = "",

  @field:NotNull(message = ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_EMAIL_REQUIRED)
  val responsibleOrganisationEmail: String = "",
) {
  @AssertTrue(message = ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_NAME_REQUIRED)
  fun isNotifyingOrganisationName(): Boolean {
    val b = MagistrateCourt.entries.any { it.name == notifyingOrganisationName }

    if (notifyingOrganisation === NotifyingOrganisation.PRISON) {
      return Prison.entries.any { it.name == notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisation.CROWN_COURT) {
      return CrownCourt.entries.any { it.name == notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisation.MAGISTRATES_COURT) {
      return MagistrateCourt.entries.any { it.name == notifyingOrganisationName }
    }

    return notifyingOrganisationName == ""
  }

  @AssertTrue(message = ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_REGION_REQUIRED)
  fun isResponsibleOrganisationRegion(): Boolean {
    if (responsibleOrganisation === ResponsibleOrganisation.PROBATION) {
      return ProbationServiceRegion.entries.any { it.name == responsibleOrganisationRegion }
    }

    if (responsibleOrganisation === ResponsibleOrganisation.YJS) {
      return YouthJusticeServiceRegions.entries.any { it.name == responsibleOrganisationRegion }
    }

    return responsibleOrganisationRegion == ""
  }
}
