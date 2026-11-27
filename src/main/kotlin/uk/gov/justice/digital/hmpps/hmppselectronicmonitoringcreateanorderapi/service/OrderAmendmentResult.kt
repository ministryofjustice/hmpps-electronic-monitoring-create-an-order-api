package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus

data class OrderAmendmentResult(val order: Order, val outcome: ProcessingStatus)
