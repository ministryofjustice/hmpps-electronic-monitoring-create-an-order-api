package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord

data class PrisonerDetails(
  val firstName: String?,
  val middleNames: String?,
  val lastName: String?,
  val dateOfBirth: String?,
  val sex: CodeDescription?,
  val identifiers: Identifiers?,
  val aliases: List<Alias>?,
  val addresses: List<Address>?,
  val religion: CodeDescription?,
  val ethnicity: CodeDescription?,
  val nationalities: List<CodeDescription>,
)
