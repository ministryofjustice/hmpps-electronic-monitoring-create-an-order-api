package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
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
      offenceType = updateRecord.offenceType,
      policeArea = updateRecord.policeArea,
    )

    clearDeselectedConditionDetails(order)

    return orderRepo.save(order).monitoringConditions!!
  }

  private fun clearDeselectedConditionDetails(order: Order) {
    val conditions = order.monitoringConditions

    if (conditions?.curfew == false) {
      clearCurfew(order)
    }
    if (conditions?.exclusionZone == false) {
      clearExclusionZone(order)
    }
    if (conditions?.trail == false) {
      clearTrail(order)
    }
    if (conditions?.mandatoryAttendance == false) {
      clearMandatoryAttendance(order)
    }
    if (conditions?.alcohol == false) {
      clearAlcohol(order)
    }
    if (!isTagAtSourceAvailable(conditions)) {
      clearTagAtSource(order)
    }
  }

  private fun clearTagAtSource(order: Order) {
    order.installationLocation = null
    order.installationAppointment = null
    order.addresses.removeAll { it.addressType == AddressType.INSTALLATION }
  }

  private fun clearAlcohol(order: Order) {
    order.monitoringConditionsAlcohol = null
  }

  private fun clearMandatoryAttendance(order: Order) {
    order.mandatoryAttendanceConditions.clear()
  }

  private fun clearTrail(order: Order) {
    order.monitoringConditionsTrail = null
  }

  private fun clearExclusionZone(order: Order) {
    order.enforcementZoneConditions.clear()
  }

  private fun clearCurfew(order: Order) {
    order.curfewConditions = null
    order.curfewReleaseDateConditions = null
    order.curfewTimeTable.clear()
  }

  fun removeMonitoringCondition(orderId: UUID, username: String, monitoringConditionId: UUID) {
    val order = this.findEditableOrder(orderId, username)

    if (isCurfew(order, monitoringConditionId)) {
      this.clearCurfew(order)
    }

    val wasZoneRemoved = order.enforcementZoneConditions.removeIf { it.id == monitoringConditionId }
    if (wasZoneRemoved) {
      // re-assign the zone ids so they remain sequential
      order.enforcementZoneConditions.forEachIndexed { index, zone -> zone.zoneId = index }
    }

    if (isTrail(order, monitoringConditionId)) {
      this.clearTrail(order)
    }

    if (isAlcohol(order, monitoringConditionId)) {
      this.clearAlcohol(order)
    }

    orderRepo.save(order)
  }

  private fun isAlcohol(order: Order, monitoringConditionId: UUID) =
    order.monitoringConditionsAlcohol?.id == monitoringConditionId

  private fun isTrail(order: Order, monitoringConditionId: UUID) =
    order.monitoringConditionsTrail?.id == monitoringConditionId

  private fun isCurfew(order: Order, monitoringConditionId: UUID) =
    order.curfewConditions?.id == monitoringConditionId ||
      order.curfewReleaseDateConditions?.id == monitoringConditionId ||
      order.curfewTimeTable.any { it.id == monitoringConditionId }
}
