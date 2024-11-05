package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception

class SubmitOrderException : RuntimeException {
  constructor(
    message: String,
  ) : super(message)
  constructor(
    message: String,
    cause: Throwable,
  ) : super(message, cause)
}
