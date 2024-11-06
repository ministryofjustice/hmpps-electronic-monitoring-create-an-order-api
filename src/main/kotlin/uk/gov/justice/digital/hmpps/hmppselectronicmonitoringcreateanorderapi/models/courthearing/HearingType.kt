package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

data class HearingType(

  val id: String,

  val description: String,

  val welshDescription: String? = null,
)
