package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import java.time.ZonedDateTime
import java.util.UUID

data class UpdateOffenceDto(
  val id: UUID? = null,
  val offenceType: String? = null,
  val offenceDate: ZonedDateTime? = null,
)
