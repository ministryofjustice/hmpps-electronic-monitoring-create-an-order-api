package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails

data class RejectedNOEmail(
  override val emailAddress: String?,
  override val dwFirstName: String?,
  override val dwLastName: String?,
  val notifyingOrgName: String?,
) : Email
