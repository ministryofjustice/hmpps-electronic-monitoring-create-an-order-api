package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails

interface Email {
  val emailAddress: String
  val dwFirstName: String?
  val dwLastName: String?
  val type: EmailType
}

enum class EmailType {
  USER,
  NO,
}
