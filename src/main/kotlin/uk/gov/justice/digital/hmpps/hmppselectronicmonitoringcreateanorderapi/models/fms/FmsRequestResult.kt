package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

data class FmsRequestResult(
  val success: Boolean,
  val error: String = "",
  val id: String = "",
  val payload: String = "",
)

data class Result<T>(
  val success: Boolean,
  val data: T? = null,
  val error: Exception? = null,
)
