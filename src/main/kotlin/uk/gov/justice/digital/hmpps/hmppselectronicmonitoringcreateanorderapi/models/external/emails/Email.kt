package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails

import java.util.UUID

sealed interface Email {
  val emailAddress: String
  val dwFirstName: String?
  val dwLastName: String?
  val orderId: UUID
  val type: EmailType
  val personalisationFields: Map<String, String?>
}

enum class EmailType {
  USER,
  NO,
}
