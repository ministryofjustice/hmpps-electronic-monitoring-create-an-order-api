package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.util.*

@Service
class MonitoringConditionsService(@Value("\${toggle.tag-at-source.enabled}") private val tagAtSourceEnabled: Boolean) :
  OrderSectionServiceBase() {

  private fun isTagAtSourceAvailable(monitoringConditions: MonitoringConditions?): Boolean {
    val isAlcohol = monitoringConditions?.alcohol == true
    return tagAtSourceEnabled || isAlcohol
  }

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
      pilot = updateRecord.pilot,
    )

    if (!isTagAtSourceAvailable(order.monitoringConditions)) {
      order.installationLocation = null
      order.installationAppointment = null
      order.monitoringConditionsAlcohol = null
      order.addresses.removeAll { it.addressType == AddressType.INSTALLATION }
    }
    return orderRepo.save(order).monitoringConditions!!
  }
}
