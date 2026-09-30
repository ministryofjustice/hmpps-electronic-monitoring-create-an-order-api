package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order

data class RejectedNOEmail(
  override val emailAddress: String,
  override val dwFirstName: String?,
  override val dwLastName: String?,
  val notifyingOrgName: String?,
) : Email {
  override val type: EmailType = EmailType.NO

  companion object {
    fun fromOrder(order: Order): RejectedNOEmail? {
      val emailAddress =
        order.interestedParties?.notifyingOrganisationEmail.takeIf { it?.isNotBlank() == true } ?: return null

      return RejectedNOEmail(
        emailAddress = emailAddress,
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        notifyingOrgName = order.interestedParties?.notifyingOrganisationName,
      )
    }
  }
}
