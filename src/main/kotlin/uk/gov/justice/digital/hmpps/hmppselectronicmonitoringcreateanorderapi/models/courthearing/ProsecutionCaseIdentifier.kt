package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProsecutionCaseIdentifier(
  val prosecutionAuthorityCode: String,

  val prosecutionAuthorityId: String,

  val caseURN: String?,
)
