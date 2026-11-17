package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.slf4j.MDC
import java.util.UUID

object CorrelationIdContext {
  const val MDC_KEY = "correlationId"
  const val HEADER_NAME = "X-Correlation-ID"

  private val safeCorrelationId = Regex("[A-Za-z0-9._-]{1,64}")

  fun current(): String? = MDC.get(MDC_KEY)

  fun newId(): String = UUID.randomUUID().toString()

  fun validOrNew(candidate: String?): String = candidate?.takeIf(safeCorrelationId::matches) ?: newId()

  fun <T> withCorrelationId(correlationId: String, action: () -> T): T {
    val previous = MDC.get(MDC_KEY)
    MDC.put(MDC_KEY, correlationId)
    return try {
      action()
    } finally {
      if (previous == null) {
        MDC.remove(MDC_KEY)
      } else {
        MDC.put(MDC_KEY, previous)
      }
    }
  }
}
