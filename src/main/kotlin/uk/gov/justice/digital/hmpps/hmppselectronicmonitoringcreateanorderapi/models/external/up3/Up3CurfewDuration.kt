package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

data class Up3CurfewDuration(var location: String, var allday: String, var schedule: List<Up3CurfewSchedule>)
