package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception

import org.springframework.http.HttpStatusCode

class CorePersonRecordDependencyException(
  message: String,
  val upstreamStatusCode: HttpStatusCode?,
  cause: Throwable? = null,
) : RuntimeException(message, cause)
