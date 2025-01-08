package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource

interface FmsSubmissionStrategy {
  fun submitOrder(order: Order, orderSource: FmsOrderSource): FmsSubmissionResult
}
