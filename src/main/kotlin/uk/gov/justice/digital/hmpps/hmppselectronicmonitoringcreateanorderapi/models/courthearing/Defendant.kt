package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Defendant(

  val id: String,

  val offences: List<Offence> = emptyList(),

  val prosecutionCaseId: String,

  val personDefendant: PersonDefendant?,

  val legalEntityDefendant: LegalEntityDefendant?,

  val masterDefendantId: String?,

  val pncId: String?,

  val croNumber: String?,
)
