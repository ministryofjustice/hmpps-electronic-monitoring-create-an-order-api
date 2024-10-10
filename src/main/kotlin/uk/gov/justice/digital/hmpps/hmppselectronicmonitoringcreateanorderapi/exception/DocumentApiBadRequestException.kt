package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

class DocumentApiBadRequestException(val error: ErrorResponse) : RuntimeException(error.userMessage)
