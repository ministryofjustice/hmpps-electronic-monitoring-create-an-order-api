package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception

class ForbiddenException(message: String, val errorCode: Int? = null) : RuntimeException(message)
