package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MonitoringConditionsRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateMonitoringConditionsDto
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
      EntityNotFoundException("Monitoring Conditions for $orderId not found")
    }

    with(monitoringConditionsUpdateRecord) {
      monitoringConditions.orderType = orderType
      monitoringConditions.devicesRequired = devicesRequired
      monitoringConditions.acquisitiveCrime = acquisitiveCrime
      monitoringConditions.dapol = dapol
      monitoringConditions.curfew = curfew
      monitoringConditions.exclusionZone = exclusionZone
      monitoringConditions.trail = trail
      monitoringConditions.mandatoryAttendance = mandatoryAttendance
      monitoringConditions.alcohol = alcohol
    }

    return repo.save(monitoringConditions)
  }
}
