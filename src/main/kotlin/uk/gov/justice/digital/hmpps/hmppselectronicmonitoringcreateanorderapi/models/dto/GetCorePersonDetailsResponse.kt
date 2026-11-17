package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import java.time.ZonedDateTime

data class GetCorePersonDetailsResponse(
  val firstName: String?,
  val lastName: String?,
  val dateOfBirth: ZonedDateTime?,
  val organisationSearchId: String,
  val addresses: List<Address>?,
)
