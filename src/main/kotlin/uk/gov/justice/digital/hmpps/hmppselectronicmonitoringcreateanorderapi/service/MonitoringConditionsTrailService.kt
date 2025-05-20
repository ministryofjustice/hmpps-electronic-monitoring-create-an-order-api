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
      endDate = getDefaultZonedDateTime(updateRecord.endDate, 23, 59),
      startDate = getDefaultZonedDateTime(updateRecord.startDate, 0, 0),
    )

    return orderRepo.save(order).monitoringConditionsTrail!!
  }
}
