package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MonitoringConditionsRepository
import java.util.*

@Service
class MonitoringConditionsService(
  val repo: MonitoringConditionsRepository,
) {
  fun updateMonitoringConditions(
    orderId: UUID,
    username: String,
    monitoringConditionsUpdateRecord: UpdateMonitoringConditionsDto,
  ): MonitoringConditions {
    val monitoringConditions = repo.findByOrderIdAndOrderUsernameAndOrderStatus(
      orderId,
      username,
      OrderStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("An editable Monitoring Conditions was not found for Order: $orderId")
    }

    with(monitoringConditionsUpdateRecord) {
      monitoringConditions.orderType = orderType
      monitoringConditions.startDate = startDate
      monitoringConditions.endDate = endDate
      monitoringConditions.orderTypeDescription = orderTypeDescription
      monitoringConditions.conditionType = conditionType
      monitoringConditions.devicesRequired = devicesRequired
      monitoringConditions.curfew = curfew
      monitoringConditions.exclusionZone = exclusionZone
      monitoringConditions.trail = trail
      monitoringConditions.mandatoryAttendance = mandatoryAttendance
      monitoringConditions.alcohol = alcohol
    }

    return repo.save(monitoringConditions)
  }
}
