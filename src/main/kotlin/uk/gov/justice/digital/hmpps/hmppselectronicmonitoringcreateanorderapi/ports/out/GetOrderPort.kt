package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.ports.out

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.domain.model.Order
import java.util.UUID

interface GetOrderPort {
  fun getOrderById(orderId: UUID, username: String): Order
}
