package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import java.util.*

class InterestedPartiesBuilder(var versionId: UUID) {
  var responsibleOfficerName: String = "John Smith"
  var responsibleOfficerPhoneNumber: String = "07401111111"
  var responsibleOrganisation: String = "PROBATION"
  var responsibleOrganisationRegion: String = "LONDON"
  var responsibleOrganisationEmail: String = "abc@def.com"
  var notifyingOrganisation: String = "PRISON"
  var notifyingOrganisationName: String = "WAYLAND_PRISON"
  var notifyingOrganisationEmail: String = ""

  fun build(): InterestedParties {
    return InterestedParties(
      versionId = versionId,
      responsibleOfficerName = responsibleOfficerName,
      responsibleOfficerPhoneNumber = responsibleOfficerPhoneNumber,
      responsibleOrganisation = responsibleOrganisation,
      responsibleOrganisationRegion = responsibleOrganisationRegion,
      responsibleOrganisationEmail = responsibleOrganisationEmail,
      notifyingOrganisation = notifyingOrganisation,
      notifyingOrganisationName = notifyingOrganisationName,
      notifyingOrganisationEmail = notifyingOrganisationEmail,
    )
  }
}
