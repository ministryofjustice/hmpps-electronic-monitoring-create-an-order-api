package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import java.util.*

@Repository
interface MonitoringConditionsTrailRepository : JpaRepository<TrailMonitoringConditions, UUID> {
  fun findByOrderIdAndOrderUsernameAndOrderStatus(
    id: UUID,
    username: String,
    status: OrderStatus,
  ): Optional<TrailMonitoringConditions>
}
