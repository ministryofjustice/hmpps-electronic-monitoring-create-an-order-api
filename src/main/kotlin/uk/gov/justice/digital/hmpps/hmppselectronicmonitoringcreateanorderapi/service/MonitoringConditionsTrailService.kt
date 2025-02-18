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
    val order = this.findEditableOrder(orderId, username)

    order.monitoringConditionsTrail = TrailMonitoringConditions(
      versionId = order.getCurrentVersion().id,
      startDate = updateRecord.startDate,
      endDate = updateRecord.endDate,
    )

    return orderRepo.save(order).monitoringConditionsTrail!!
  }
}
