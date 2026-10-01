package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import java.util.UUID

data class RejectedNOEmail(
  override val emailAddress: String,
  override val dwFirstName: String?,
  override val dwLastName: String?,
  override val orderId: UUID,
  val notifyingOrgName: String?,
) : Email {
  override val type: EmailType = EmailType.NO

  override val personalisationFields: Map<String, String?>
    get() = mapOf(
      "dwFirstName" to dwFirstName,
      "dwLastName" to dwLastName,
      "orderId" to orderId.toString(),
      "notifyingOrgName" to notifyingOrgName,
    )

  companion object {
    fun fromOrder(order: Order): RejectedNOEmail? {
      val emailAddress =
        order.interestedParties?.notifyingOrganisationEmail.takeIf { it?.isNotBlank() == true } ?: return null

      return RejectedNOEmail(
        emailAddress = emailAddress,
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        orderId = order.id,
        notifyingOrgName = order.interestedParties?.notifyingOrganisationName,
      )
    }
  }
}
