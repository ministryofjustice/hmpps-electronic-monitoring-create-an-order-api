package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.validation.constraints.AssertTrue
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RiskCategory

data class UpdateDetailsOfInstallationDto(val riskCategory: Array<String>? = null, val riskDetails: String? = "") {
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
