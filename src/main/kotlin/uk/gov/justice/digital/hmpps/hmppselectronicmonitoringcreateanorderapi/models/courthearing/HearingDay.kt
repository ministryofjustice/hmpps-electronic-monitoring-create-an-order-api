package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class HearingDay(val sittingDay: LocalDateTime, val listedDurationMinutes: Int, val listingSequence: Int)
