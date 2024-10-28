package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

data class Offence(
  val id: String,

  val offenceDefinitionId: String,

  val offenceCode: String,

  val offenceTitle: String,

  val wording: String,

  val offenceLegislation: String? = null,

  val listingNumber: Int? = null,

  val judicialResults: List<JudicialResults> = emptyList(),

  val plea: Plea?,

  val verdict: Verdict?,

)
