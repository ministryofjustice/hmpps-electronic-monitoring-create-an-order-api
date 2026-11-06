package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception

class OrderChangeException(val errorCode: String, override val message: String) : RuntimeException(message)
