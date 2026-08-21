package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord

data class Address(
  val noFixedAbode: Boolean?,
  val postcode: String?,
  val status: CodeDescription?,
  val buildingNumber: String?,
  val buildingName: String?,
  val subBuildingName: String?,
  val postTown: String?,
  val county: String?,
  val thoroughfareName: String?,
  val contacts: List<Contact>,
)
