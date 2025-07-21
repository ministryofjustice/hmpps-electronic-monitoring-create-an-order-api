package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.util.UUID

class AddressBuilder(var versionId: UUID) {
  var addressLine1: String = ""
  var addressLine2: String = ""
  var addressLine3: String = ""
  var addressLine4: String = ""
  var postcode: String = ""
  var addressType: AddressType? = null

  fun build(): Address {
    require(addressLine1.isNotBlank()) { "AddressLine1 cannot be blank" }
    require(addressLine2.isNotBlank()) { "AddressLine2 cannot be blank" }
    require(postcode.isNotBlank()) { "Postcode cannot be blank" }
    require(addressType != null) { "AddressType must be specified" }

    return Address(
      versionId = versionId,
      addressLine1 = addressLine1,
      addressLine2 = addressLine2,
      addressLine3 = addressLine3,
      addressLine4 = addressLine4,
      postcode = postcode,
      addressType = addressType!!,
    )
  }
}