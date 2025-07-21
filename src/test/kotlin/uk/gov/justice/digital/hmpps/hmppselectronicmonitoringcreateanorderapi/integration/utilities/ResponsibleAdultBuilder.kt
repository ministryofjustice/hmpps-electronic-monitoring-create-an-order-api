package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import java.util.*

class ResponsibleAdultBuilder(var versionId: UUID) {
  var fullName = "Mark Smith"
  var contactNumber = "+44740111111"

  fun build(): ResponsibleAdult {
    return ResponsibleAdult(versionId = versionId, fullName = fullName, contactNumber = contactNumber)
  }
}