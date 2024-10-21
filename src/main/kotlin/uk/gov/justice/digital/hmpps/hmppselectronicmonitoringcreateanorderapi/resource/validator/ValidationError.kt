package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator

data class ValidationError(val field: String, val error: String)

data class ListItemValidationError(val errors: List<ValidationError>, val index: Int)
