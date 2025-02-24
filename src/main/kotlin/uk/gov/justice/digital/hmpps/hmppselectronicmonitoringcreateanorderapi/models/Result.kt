package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

data class Result<T>(val success: Boolean, val data: T? = null, val error: Exception? = null)
