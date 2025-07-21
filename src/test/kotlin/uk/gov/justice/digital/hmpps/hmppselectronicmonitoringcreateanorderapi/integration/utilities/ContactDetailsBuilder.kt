package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import java.util.UUID

class ContactDetailsBuilder(var versionId: UUID) {
  var contactNumber: String = "07401111111"

  fun build(): ContactDetails {
    return ContactDetails(versionId = versionId, contactNumber = contactNumber)
  }
}