package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import java.time.ZonedDateTime

data class UpdateOffenceDto(val offenceType: String? = null, val offenceDate: ZonedDateTime? = null)
