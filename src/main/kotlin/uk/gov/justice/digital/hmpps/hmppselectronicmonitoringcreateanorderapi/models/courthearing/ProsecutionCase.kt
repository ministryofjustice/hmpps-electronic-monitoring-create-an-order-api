package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProsecutionCase(

  val id: String,

  val initiationCode: InitiationCode,

  val prosecutionCaseIdentifier: ProsecutionCaseIdentifier,

  val defendants: List<Defendant> = emptyList(),

  val caseStatus: String?,

  val caseMarkers: List<CaseMarker> = emptyList(),

)
enum class InitiationCode(val description: String) {
  J("SJP Notice"),
  Q("Requisition"),
  S("Summons"),
  C("Charge"),
  R("Remitted"),
  O("Other"),
  Z("SJP Referral"),
}
