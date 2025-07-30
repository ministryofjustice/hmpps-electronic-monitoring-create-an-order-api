package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CivilCountyCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FamilyCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MagistrateCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MilitaryCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.PrisonDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthCustodyServiceRegionDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthJusticeServiceRegions

data class UpdateInterestedPartiesDto(
  @field:NotNull(message = ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_REQUIRED)
  val notifyingOrganisation: NotifyingOrganisationDDv5? = null,

  @field:NotNull(message = ValidationErrors.InterestedParties.TEAM_EMAIL_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.InterestedParties.TEAM_EMAIL_MAX_LENGTH)
  val notifyingOrganisationEmail: String = "",

  val notifyingOrganisationName: String = "",

  @field:NotEmpty(message = ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_FULL_NAME_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_NAME_MAX_LENGTH)
  val responsibleOfficerName: String = "",

  @field:NotEmpty(message = ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_TELEPHONE_NUMBER_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.InterestedParties.RESPONSIBLE_OFFICER_TELEPHONE_NUMBER_MAX_LENGTH)
  val responsibleOfficerPhoneNumber: String? = null,

  @field:NotNull(message = ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_REQUIRED)
  val responsibleOrganisation: ResponsibleOrganisation? = null,

  val responsibleOrganisationRegion: String = "",

  @field:NotNull(message = ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_EMAIL_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.InterestedParties.RESPONSIBLE_ORGANISATION_EMAIL_MAX_LENGTH)
  val responsibleOrganisationEmail: String = "",
) {
  @AssertTrue(message = ValidationErrors.InterestedParties.NOTIFYING_ORGANISATION_NAME_REQUIRED)
  fun isNotifyingOrganisationName(): Boolean {
    val b = MagistrateCourtDDv5.entries.any { it.name == notifyingOrganisationName }

    if (notifyingOrganisation === NotifyingOrganisationDDv5.PRISON) {
      return PrisonDDv5.entries.any { it.name == notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisationDDv5.CROWN_COURT) {
      return CrownCourtDDv5.entries.any { it.name == notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisationDDv5.MAGISTRATES_COURT) {
      return MagistrateCourtDDv5.entries.any { it.name == notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisationDDv5.CIVIL_COUNTY_COURT) {
      return CivilCountyCourtDDv5.entries.any { it.name == notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisationDDv5.MILITARY_COURT) {
      return notifyingOrganisationName == "" || MilitaryCourtDDv5.entries.any { it.name == notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE) {
      return notifyingOrganisationName == "" ||
        YouthCustodyServiceRegionDDv5.entries.any { it.name == notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisationDDv5.YOUTH_COURT) {
      return notifyingOrganisationName == "" || YouthCourtDDv5.entries.any { it.name == notifyingOrganisationName }
    }

    // NB: The setters below are for DDv5. Remove 'notifyingOrganisationName == ""' from the return clause to make the organisation name mandatory for these organisation types.
    if (notifyingOrganisation === NotifyingOrganisationDDv5.FAMILY_COURT) {
      return notifyingOrganisationName == "" || FamilyCourtDDv5.entries.any { it.name == notifyingOrganisationName }
    }

    if (notifyingOrganisation === NotifyingOrganisationDDv5.PROBATION) {
      return notifyingOrganisationName == "" ||
        ProbationServiceRegion.entries.any { it.name == notifyingOrganisationName }
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
