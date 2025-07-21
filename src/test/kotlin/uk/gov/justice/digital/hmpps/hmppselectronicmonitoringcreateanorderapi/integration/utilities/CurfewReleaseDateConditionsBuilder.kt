package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.time.ZonedDateTime
import java.util.*

class CurfewReleaseDateConditionsBuilder(var versionId: UUID) {
  var releaseDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  var startTime: String = "19:00"
  var endTime: String = "23:00"
  var curfewAddress: AddressType = AddressType.PRIMARY

  fun build(): CurfewReleaseDateConditions {
    return CurfewReleaseDateConditions(
      versionId = versionId,
      releaseDate = releaseDate,
      startTime = startTime,
      endTime = endTime,
      curfewAddress = curfewAddress,
    )
  }
}
