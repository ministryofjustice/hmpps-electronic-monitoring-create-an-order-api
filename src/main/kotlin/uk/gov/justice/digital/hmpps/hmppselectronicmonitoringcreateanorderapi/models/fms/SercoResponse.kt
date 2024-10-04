package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

class SercoResponse(
  val result: List<SercoResult> = emptyList(),
  val status: String? = null,
)

class SercoResult(
  val message: String? = null,
  val id: String? = null,
)
