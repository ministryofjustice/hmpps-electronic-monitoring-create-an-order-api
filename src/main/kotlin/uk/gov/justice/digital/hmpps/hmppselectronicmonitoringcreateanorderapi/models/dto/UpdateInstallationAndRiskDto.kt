package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

data class UpdateInstallationAndRiskDto(
  val offence: String? = "",

  val riskCategory: Array<String>? = null,

  val riskDetails: String? = "",

  val mappaLevel: String? = "",

  val mappaCaseType: String? = "",
)
