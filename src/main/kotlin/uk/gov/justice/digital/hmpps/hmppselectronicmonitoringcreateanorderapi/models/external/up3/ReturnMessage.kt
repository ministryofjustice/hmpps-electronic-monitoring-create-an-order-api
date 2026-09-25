package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import com.fasterxml.jackson.annotation.JsonProperty

data class ReturnMessage(
  val caseId: String,
  val status: ReturnStatus,
  val reasons: List<Reason>,
  val datetimeOfStatusChange: String,
)

data class Reason(val section: String, val details: String)

enum class ReturnStatus {
  @JsonProperty("rejected")
  REJECTED,
}
