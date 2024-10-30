package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateTrailMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MonitoringConditionsTrailRepository
import java.util.*

@Service
class MonitoringConditionsTrailService(

  val trailMonitoringConditionsRepo: MonitoringConditionsTrailRepository,
) : OrderSectionServiceBase() {
  fun getTrailMonitoringConditions(orderId: UUID, username: String): TrailMonitoringConditions {
    // Verify the order belongs to the user and is in draft state
    val order = findEditableOrder(orderId, username)

    // Find an existing trail monitoring conditions or create new trail monitoring conditions
    return order.monitoringConditionsTrail ?: TrailMonitoringConditions(orderId = orderId)
  }

  fun createOrUpdateTrailMonitoringConditions(
    orderId: UUID,
    username: String,
    trailMonitoringConditionsUpdateRecord: UpdateTrailMonitoringConditionsDto,
  ): TrailMonitoringConditions {
    val trailMonitoringConditions = this.getTrailMonitoringConditions(
      orderId,
      username,
    )

    with(trailMonitoringConditionsUpdateRecord) {
      trailMonitoringConditions.startDate = startDate
      trailMonitoringConditions.endDate = endDate
    }

    return trailMonitoringConditionsRepo.save(trailMonitoringConditions)
  }
}
