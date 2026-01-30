package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.ports.out

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.domain.model.Order

interface UpdateOrderPort {
  fun updateOrder(order: Order)
}
