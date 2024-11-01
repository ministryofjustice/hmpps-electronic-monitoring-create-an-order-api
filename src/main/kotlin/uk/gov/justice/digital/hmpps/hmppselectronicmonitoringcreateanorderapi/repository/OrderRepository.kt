package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
  fun findByUsername(
    username: String,
  ): List<Order>

  fun findByUsernameAndId(
    username: String,
    id: UUID,
  ): Order?

  fun findByIdAndUsernameAndStatus(
    id: UUID,
    username: String,
    status: OrderStatus,
  ): Optional<Order>
}
