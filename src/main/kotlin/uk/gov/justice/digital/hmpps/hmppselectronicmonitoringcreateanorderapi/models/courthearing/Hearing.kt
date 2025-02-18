package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.HearingEventHandler

@JsonIgnoreProperties(ignoreUnknown = true)
data class Hearing(

  val id: String,

  val courtCentre: CourtCentre,

  val type: HearingType,

  val jurisdictionType: JurisdictionType,

  val hearingDays: List<HearingDay> = emptyList(),

  val prosecutionCases: List<ProsecutionCase> = emptyList(),
) {

  fun isHearingContainsEM(): Boolean {
    return this.prosecutionCases.any {
        case ->
      case.defendants.any {
          defendant ->
        defendant.offences.any {
            offence ->
          HearingEventHandler.isEnglandAdnWalesEMRequest(offence)
        }
      }
    }
  }
}

enum class JurisdictionType(val value: String) {
  MAGISTRATES("Magistrates Court"),
  CROWN("Crown Court"),
}
