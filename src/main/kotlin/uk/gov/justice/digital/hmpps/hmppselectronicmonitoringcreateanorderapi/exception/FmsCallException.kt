package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception

enum class FmsFailureCategory(val telemetryValue: String) {
  // TODO: TBC
  HTTP_ERROR("http_error"),
  TIMEOUT("timeout"),
  TRANSPORT_ERROR("transport_error"),
  RESPONSE_ERROR("response_error"),
  CLIENT_ERROR("client_error"),
}

class FmsCallException(
  val operation: String,
  val failureCategory: FmsFailureCategory,
  val httpStatusCode: Int? = null,
  message: String,
  cause: Throwable? = null,
) : CreateSercoEntityException(message, cause)
