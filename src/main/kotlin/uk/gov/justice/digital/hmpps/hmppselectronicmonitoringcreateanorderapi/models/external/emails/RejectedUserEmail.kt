package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails

data class RejectedUserEmail(
  override val emailAddress: String?,
  override val dwFirstName: String?,
  override val dwLastName: String?,
  val userFirstName: String?,
  val userLastName: String?,
) : Email
