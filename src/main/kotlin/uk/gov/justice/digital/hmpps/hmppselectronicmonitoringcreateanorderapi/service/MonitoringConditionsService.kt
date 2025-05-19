package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import java.time.ZonedDateTime
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
      startDate = ZonedDateTime.of(
        updateRecord.startDate!!.year,
        updateRecord.startDate.monthValue,
        updateRecord.startDate.dayOfMonth,
        0,
        0,
        0,
        0,
        updateRecord.startDate.zone,
      ),
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
    if (updateRecord.endDate != null) {
      order.monitoringConditions!!.endDate = ZonedDateTime.of(
        updateRecord.endDate.year,
        updateRecord.endDate.monthValue,
        updateRecord.endDate.dayOfMonth,
        23,
        59,
        0,
        0,
        updateRecord.startDate.zone,
      )
    }

    return orderRepo.save(order).monitoringConditions!!
  }
}
