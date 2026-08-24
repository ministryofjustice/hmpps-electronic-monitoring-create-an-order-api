package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import java.time.ZonedDateTime

data class GetPrisonDetailsResponse(
  val firstName: String?,
  val lastName: String?,
  val dateOfBirth: ZonedDateTime?,
  val prisonNumber: String,
)
