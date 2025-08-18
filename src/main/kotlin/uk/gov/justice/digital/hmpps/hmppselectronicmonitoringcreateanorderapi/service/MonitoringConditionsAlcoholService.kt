package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateAlcoholMonitoringConditionsDto
import java.util.*

@Service
class MonitoringConditionsAlcoholService : OrderSectionServiceBase() {
  fun createOrUpdateAlcoholMonitoringConditions(
    orderId: UUID,
    username: String,
    alcoholMonitoringConditionsUpdateRecord: UpdateAlcoholMonitoringConditionsDto,
  ): AlcoholMonitoringConditions {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    // Find existing alcohol monitoring conditions or create new alcohol monitoring conditions
    val alcoholMonitoringConditions =
      order.monitoringConditionsAlcohol ?: AlcoholMonitoringConditions(versionId = order.getCurrentVersion().id)

    with(alcoholMonitoringConditionsUpdateRecord) {
      alcoholMonitoringConditions.monitoringType = monitoringType
      alcoholMonitoringConditions.startDate = startDate
      alcoholMonitoringConditions.endDate = endDate
    }

    order.monitoringConditionsAlcohol = alcoholMonitoringConditions

    return orderRepo.save(order).monitoringConditionsAlcohol!!
  }
}
