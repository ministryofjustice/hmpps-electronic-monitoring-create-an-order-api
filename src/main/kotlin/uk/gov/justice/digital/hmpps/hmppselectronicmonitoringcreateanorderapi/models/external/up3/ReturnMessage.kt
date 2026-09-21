package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

data class ReturnMessage(
  val caseId: String,
  val status: String,
  val reasons: List<Reason>,
  val datetimeOfStatusChange: String,
)

data class Reason(val section: String, val details: String)
