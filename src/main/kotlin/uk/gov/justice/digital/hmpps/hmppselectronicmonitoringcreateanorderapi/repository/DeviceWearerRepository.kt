package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import java.util.*

@Repository
interface DeviceWearerRepository : JpaRepository<DeviceWearer, UUID> {
  fun findByOrderIdAndOrderUsername(id: UUID, username: String): Optional<DeviceWearer>

  fun findByOrderIdAndOrderUsernameAndOrderStatus(
    id: UUID,
    username: String,
    status: OrderStatus,
  ): Optional<DeviceWearer>
}
