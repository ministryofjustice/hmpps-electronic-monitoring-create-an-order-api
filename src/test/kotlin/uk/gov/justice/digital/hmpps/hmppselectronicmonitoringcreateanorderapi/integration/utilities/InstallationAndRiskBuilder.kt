package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import java.util.UUID

class InstallationAndRiskBuilder(var versionId: UUID) {
  var offence: String = "FRAUD_OFFENCES"
  var riskDetails: String = "Danger"
  var riskCategory: Array<String> = arrayOf("SEXUAL_OFFENCES", "RISK_TO_GENDER")
  var mappaLevel: String = "MAPPA 1"
  var mappaCaseType: String? = "CPPC (Critical Public Protection Case)"

  fun build(): InstallationAndRisk {
    return InstallationAndRisk(
      versionId = versionId,
      offence = offence,
      riskDetails = riskDetails,
      riskCategory = riskCategory,
      mappaLevel = mappaLevel,
      mappaCaseType = mappaCaseType,
    )
  }
}