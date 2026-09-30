package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order

data class RejectedUserEmail(
  override val emailAddress: String,
  override val dwFirstName: String?,
  override val dwLastName: String?,
  val userFirstName: String?,
  val userLastName: String?,
) : Email {
  companion object {
    fun fromOrder(order: Order): RejectedUserEmail? {
      val emailAddress = order.submittedByEmail?.takeIf { it.isNotBlank() } ?: return null

      return RejectedUserEmail(
        emailAddress = emailAddress,
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        userFirstName = order.submittedBy?.split(" ")?.first(),
        userLastName = order.submittedBy?.split(" ")?.last(),
      )
    }
  }
}
