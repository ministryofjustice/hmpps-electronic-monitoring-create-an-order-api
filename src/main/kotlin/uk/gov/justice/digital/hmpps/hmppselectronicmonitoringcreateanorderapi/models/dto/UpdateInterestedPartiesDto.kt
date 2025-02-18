package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto
import jakarta.validation.constraints.AssertTrue
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
  @field:NotNull(message = ValidationErrors.NOTIFYING_ORGANISATION_REQUIRED)
  val notifyingOrganisation: NotifyingOrganisation? = null,

  val notifyingOrganisationEmail: String = "",

  val notifyingOrganisationName: String = "",

  val responsibleOfficerName: String = "",

  val responsibleOfficerPhoneNumber: String? = null,

  @field:NotNull(message = ValidationErrors.RESPONSIBLE_ORGANISATION_REQUIRED)
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
  @AssertTrue(message = ValidationErrors.NOTIFYING_ORGANISATION_NAME_REQUIRED)
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

  @AssertTrue(message = ValidationErrors.RESPONSIBLE_ORGANISATION_REGION_REQUIRED)
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
