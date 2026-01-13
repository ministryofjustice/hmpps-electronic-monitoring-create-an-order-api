package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

data class UserCaseLoad(
  val username: String,
  val active: Boolean,
  val accountType: String,
  val activeCaseload: CaseLoad,
  val caseLoads: List<CaseLoad>,
)

data class CaseLoad(val id: String, val name: String)
