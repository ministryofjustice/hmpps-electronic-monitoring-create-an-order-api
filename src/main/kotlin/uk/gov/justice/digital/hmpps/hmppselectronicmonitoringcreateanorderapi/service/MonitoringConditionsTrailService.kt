package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MonitoringConditionsTrailRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateTrailMonitoringConditionsDto
import java.util.*

@Service
class MonitoringConditionsTrailService(
  val orderRepo: OrderRepository,
  val trailMonitoringConditionsRepo: MonitoringConditionsTrailRepository,
) {
  fun getTrailMonitoringConditions(orderId: UUID, username: String): TrailMonitoringConditions {
    // Verify the order belongs to the user and is in draft state
    val order = orderRepo.findByIdAndUsernameAndStatus(
      orderId,
      username,
      OrderStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("An editable order with $orderId does not exist")
    }

    // Find an existing trail monitoring conditions or create new trail monitoring conditions
    return trailMonitoringConditionsRepo.findByOrderIdAndOrderUsernameAndOrderStatus(
      order.id,
      order.username,
      order.status,
    ).orElse(
      TrailMonitoringConditions(
        orderId = orderId,
      ),
    )
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
