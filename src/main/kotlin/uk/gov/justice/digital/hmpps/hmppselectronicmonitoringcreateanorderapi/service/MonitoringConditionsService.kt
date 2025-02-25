package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import java.util.*

@Service
class MonitoringConditionsService : OrderSectionServiceBase() {
  fun updateMonitoringConditions(
    orderId: UUID,
    username: String,
    updateRecord: UpdateMonitoringConditionsDto,
  ): MonitoringConditions {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    order.monitoringConditions = MonitoringConditions(
      versionId = order.getCurrentVersion().id,
      orderType = updateRecord.orderType,
      startDate = updateRecord.startDate,
      endDate = updateRecord.endDate,
      orderTypeDescription = updateRecord.orderTypeDescription,
      conditionType = updateRecord.conditionType,
      curfew = updateRecord.curfew,
      exclusionZone = updateRecord.exclusionZone,
      trail = updateRecord.trail,
      mandatoryAttendance = updateRecord.mandatoryAttendance,
      alcohol = updateRecord.alcohol,
      sentenceType = updateRecord.sentenceType,
      issp = updateRecord.issp,
      hdc = updateRecord.hdc,
      prarr = updateRecord.prarr,
    )

    return orderRepo.save(order).monitoringConditions!!
  }
}
