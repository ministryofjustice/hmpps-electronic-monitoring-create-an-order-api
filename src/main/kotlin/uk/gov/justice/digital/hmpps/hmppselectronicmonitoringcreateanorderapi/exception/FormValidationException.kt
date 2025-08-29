package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError

class FormValidationException(val errors: List<ValidationError>) :
  RuntimeException(
    errors.joinToString("; ") { e -> "${e.field}: ${e.error}" },
  ) {
  constructor(field: String, message: String) :
    this(listOf(ValidationError(field, message)))
}
