package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components

data class CurfewSchedule(
  val location: String? = "",

  val allday: String? = "",

  val schedule: MutableList<Schedule>? = mutableListOf(),
)
