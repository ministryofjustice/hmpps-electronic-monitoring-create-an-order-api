package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.interfaces

import java.time.ZonedDateTime

interface MonitoringCondition {
  var startDate: ZonedDateTime?
  var endDate: ZonedDateTime?
}
