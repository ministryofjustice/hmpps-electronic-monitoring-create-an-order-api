package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

data class UserCaseLoad(
  val username: String = "",
  val active: Boolean,
  val accountType: String = "",
  val activeCaseload: CaseLoad? = null,
  val caseloads: List<CaseLoad> = emptyList(),
)

data class CaseLoad(val id: String, val name: String)
