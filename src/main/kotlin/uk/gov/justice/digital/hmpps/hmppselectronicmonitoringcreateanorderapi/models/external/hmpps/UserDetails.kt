package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps

data class UserDetails(
  val username: String,
  val active: Boolean,
  val name: String,
  val authSource: String,
  val userId: String,
  val uuid: String?,
)
