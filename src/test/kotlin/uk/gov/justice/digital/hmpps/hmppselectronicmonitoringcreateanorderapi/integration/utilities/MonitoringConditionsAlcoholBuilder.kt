package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import java.time.ZonedDateTime
import java.util.*

class MonitoringConditionsAlcoholBuilder(var versionId: UUID) {
  var startDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  var endDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  var monitoringType: AlcoholMonitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE
  var installationLocation: InstallationLocationType = InstallationLocationType.PRIMARY

  fun build(): AlcoholMonitoringConditions {
    return AlcoholMonitoringConditions(
      versionId = versionId,
      startDate = startDate,
      endDate = endDate,
      monitoringType = monitoringType,
      installationLocation = installationLocation,
    )
  }
}
