package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.JudicialResultsPrompt
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class JudicialResults(
  val isConvictedResult: Boolean,

  val label: String,

  val judicialResultTypeId: String?,

  val resultText: String?,

  @JsonProperty("orderedDate")
  val orderedDate: LocalDate?,

  @JsonProperty("judicialResultPrompts")
  val judicialResultPrompts: List<JudicialResultsPrompt> = emptyList(),
)
