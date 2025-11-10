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
      clearExclusionZoneData(order)
    }
    if (conditions?.trail == false) {
      clearTrailData(order)
    }
    if (conditions?.mandatoryAttendance == false) {
      clearMandatoryAttendanceData(order)
    }
    if (conditions?.alcohol == false) {
      clearAlcoholData(order)
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

  private fun clearAlcoholData(order: Order) {
    order.monitoringConditionsAlcohol = null
  }

  private fun clearMandatoryAttendanceData(order: Order) {
    order.mandatoryAttendanceConditions.clear()
  }

  private fun clearTrailData(order: Order) {
    order.monitoringConditionsTrail = null
  }

  private fun clearExclusionZoneData(order: Order) {
    order.enforcementZoneConditions.clear()
  }

  private fun clearCurfew(order: Order) {
    order.curfewConditions = null
    order.curfewReleaseDateConditions = null
    order.curfewTimeTable.clear()
  }

  fun removeMonitoringCondition(orderId: UUID, username: String, monitoringConditionId: UUID) {
    val order = this.findEditableOrder(orderId, username)

    when {
      idMatchesCurfew(order, monitoringConditionId) -> {
        order.monitoringConditions?.curfew = false
        this.clearCurfew(order)
      }

      idMatchesTrail(order, monitoringConditionId) -> {
        order.monitoringConditions?.trail = false
        this.clearTrailData(order)
      }

      idMatchesAlcohol(order, monitoringConditionId) -> {
        order.monitoringConditions?.alcohol = false
        this.clearAlcoholData(order)
      }

      order.enforcementZoneConditions.removeIf { it.id == monitoringConditionId } -> {
        order.enforcementZoneConditions.forEachIndexed { index, zone -> zone.zoneId = index }
        if (order.enforcementZoneConditions.isEmpty()) {
          order.monitoringConditions?.exclusionZone = false
        }
      }

      order.mandatoryAttendanceConditions.removeIf { it.id == monitoringConditionId } -> {
        if (order.mandatoryAttendanceConditions.isEmpty()) {
          order.monitoringConditions?.mandatoryAttendance = false
        }
      }
    }

    orderRepo.save(order)
  }

  private fun idMatchesAlcohol(order: Order, monitoringConditionId: UUID) =
    order.monitoringConditionsAlcohol?.id == monitoringConditionId

  private fun idMatchesTrail(order: Order, monitoringConditionId: UUID) =
    order.monitoringConditionsTrail?.id == monitoringConditionId

  private fun idMatchesCurfew(order: Order, monitoringConditionId: UUID) =
    order.curfewConditions?.id == monitoringConditionId ||
      order.curfewReleaseDateConditions?.id == monitoringConditionId ||
      order.curfewTimeTable.any { it.id == monitoringConditionId }
}
