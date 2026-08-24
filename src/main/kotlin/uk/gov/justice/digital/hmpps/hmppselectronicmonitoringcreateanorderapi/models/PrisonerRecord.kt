package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

data class PrisonerRecord(
  val deviceWearer: DeviceWearer?,
  val contactDetails: ContactDetails?,
  val addresses: List<Address>,
)
