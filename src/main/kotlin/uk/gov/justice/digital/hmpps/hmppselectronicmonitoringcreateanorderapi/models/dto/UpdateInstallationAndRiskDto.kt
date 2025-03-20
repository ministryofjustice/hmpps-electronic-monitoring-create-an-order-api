package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RiskCategory

data class UpdateInstallationAndRiskDto(
  val offence: String? = "",

  val riskCategory: Array<String>? = null,

  val riskDetails: String? = "",

  val mappaLevel: String? = "",

  val mappaCaseType: String? = "",
) {
  @AssertTrue(message = ValidationErrors.InstallationAndRisk.OFFENCE_VALID)
  fun isOffence(): Boolean {
    if (offence == null) {
      return true
    }

    return Offence.entries.any { it.name == offence }
  }

  @AssertTrue(message = ValidationErrors.InstallationAndRisk.RISK_CATEGORY_VALID)
  fun isRiskCategory(): Boolean {
    if (riskCategory == null) {
      return true
    }

    return riskCategory.all {
      RiskCategory.entries.any { riskCategory -> riskCategory.name == it }
    }
  }
}
