package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressSource

data class CorePersonRecord(
  val deviceWearer: DeviceWearer?,
  val contactDetails: ContactDetails?,
  val addresses: List<Address>,
)

fun CorePersonRecord.setAddressSource(addressSource: AddressSource): CorePersonRecord =
  copy(addresses = addresses.map { it.copy(addressSource = addressSource) })
