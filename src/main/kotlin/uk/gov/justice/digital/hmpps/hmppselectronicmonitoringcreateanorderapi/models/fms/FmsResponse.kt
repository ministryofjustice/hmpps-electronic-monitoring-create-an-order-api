package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

class FmsResponse(val result: List<FmsResult> = emptyList(), val status: String? = null)

class FmsResult(val message: String? = null, val id: String? = null)

class FmsErrorResponse(val error: ErrorResponse? = null, val status: String? = null)
class ErrorResponse(val message: String? = null, val detail: String? = null)
