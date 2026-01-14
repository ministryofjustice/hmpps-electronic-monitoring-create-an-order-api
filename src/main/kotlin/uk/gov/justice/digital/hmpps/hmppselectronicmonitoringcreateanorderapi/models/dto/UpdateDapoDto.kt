package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import java.time.ZonedDateTime
import java.util.UUID

data class UpdateDapoDto(val id: UUID? = null, val clause: String? = null, val date: ZonedDateTime? = null)
