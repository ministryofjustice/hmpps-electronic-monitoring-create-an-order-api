package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import java.time.ZonedDateTime

data class GetCorePersonDetailsResponse(
  val firstName: String?,
  val lastName: String?,
  val dateOfBirth: ZonedDateTime?,
  val organisationSearchId: String,
)
