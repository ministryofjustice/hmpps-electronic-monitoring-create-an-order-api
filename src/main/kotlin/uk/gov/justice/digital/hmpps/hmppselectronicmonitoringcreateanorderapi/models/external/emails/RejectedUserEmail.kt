package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import java.util.UUID

data class RejectedUserEmail(
  override val emailAddress: String,
  override val dwFirstName: String?,
  override val dwLastName: String?,
  override val orderId: UUID,
  val username: String?,
) : Email {
  override val type: EmailType = EmailType.USER

  override val personalisationFields: Map<String, String?>
    get() = mapOf(
      "dwFirstName" to dwFirstName,
      "dwLastName" to dwLastName,
      "orderId" to orderId.toString(),
      "username" to username,
    )

  companion object {
    fun fromOrder(order: Order): RejectedUserEmail? {
      val emailAddress = order.submittedByEmail?.takeIf { it.isNotBlank() } ?: return null

      return RejectedUserEmail(
        emailAddress = emailAddress,
        dwFirstName = order.deviceWearer?.firstName,
        dwLastName = order.deviceWearer?.lastName,
        orderId = order.id,
        username = order.submittedBy,
      )
    }
  }
}
