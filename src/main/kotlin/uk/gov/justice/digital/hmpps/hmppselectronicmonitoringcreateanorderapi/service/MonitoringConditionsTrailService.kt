package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateTrailMonitoringConditionsDto
import java.util.*

@Service
class MonitoringConditionsTrailService : OrderSectionServiceBase() {
  fun updateTrailMonitoringConditions(
    orderId: UUID,
    username: String,
    updateRecord: UpdateTrailMonitoringConditionsDto,
  ): TrailMonitoringConditions {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    order.monitoringConditionsTrail = TrailMonitoringConditions(
      orderId = orderId,
      startDate = updateRecord.startDate,
      endDate = updateRecord.endDate,
    )

    return orderRepo.save(order).monitoringConditionsTrail!!
  }
}
