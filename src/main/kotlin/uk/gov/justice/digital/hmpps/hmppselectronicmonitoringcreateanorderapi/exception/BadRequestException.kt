package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception

class BadRequestException : RuntimeException {
  constructor(
    message: String,
  ) : super(message)
  constructor(
    message: String,
    cause: Throwable,
  ) : super(message, cause)
}
