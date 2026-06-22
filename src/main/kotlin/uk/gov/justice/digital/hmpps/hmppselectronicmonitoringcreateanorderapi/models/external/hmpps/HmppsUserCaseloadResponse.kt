package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps

data class HmppsUserCaseloadResponse(
  val username: String = "",
  val active: Boolean,
  val accountType: String = "",
  val activeCaseload: HmppsCaseload? = null,
  val caseloads: List<HmppsCaseload> = emptyList(),
)

data class HmppsCaseload(val id: String, val name: String)
