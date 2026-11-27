package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.interfaces.MonitoringCondition
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
      pilot = updateRecord.pilot,
      offenceType = updateRecord.offenceType,
      policeArea = updateRecord.policeArea,
      dapolMissedInError = updateRecord.dapolMissedInError,
    )

    clearDeselectedConditionDetails(order)
    order.recalculateMonitoringStartEndDate()

    return updateLastUpdatedByAndSaveOrder(order).monitoringConditions!!
  }

  fun applyImportedConditions(order: Order, conditions: List<MonitoringCondition>) {
    if (conditions.isEmpty()) return

    val parent = order.monitoringConditions
      ?: MonitoringConditions(versionId = order.versionId).also { order.monitoringConditions = it }

    conditions.filterIsInstance<CurfewConditions>().firstOrNull()?.let { imported ->
      val existing = order.curfewConditions
      if (existing == null) {
        order.curfewConditions = imported
      } else {
        existing.startDate = imported.startDate
        existing.endDate = imported.endDate
        existing.curfewDescription = imported.curfewDescription
      }
      parent.curfew = true
    }

    conditions.filterIsInstance<TrailMonitoringConditions>().firstOrNull()?.let { imported ->
      val existing = order.monitoringConditionsTrail
      if (existing == null) {
        order.monitoringConditionsTrail = imported
      } else {
        existing.startDate = imported.startDate
        existing.endDate = imported.endDate
      }
      parent.trail = true
    }

    conditions.filterIsInstance<AlcoholMonitoringConditions>().firstOrNull()?.let { imported ->
      val existing = order.monitoringConditionsAlcohol
      if (existing == null) {
        order.monitoringConditionsAlcohol = imported
      } else {
        existing.startDate = imported.startDate
        existing.endDate = imported.endDate
        existing.monitoringType = imported.monitoringType
      }
      parent.alcohol = true
    }

    val zones = conditions.filterIsInstance<EnforcementZoneConditions>()
    if (zones.isNotEmpty()) {
      order.enforcementZoneConditions.clear()
      zones.forEachIndexed { index, zone ->
        zone.zoneId = index
        order.enforcementZoneConditions.add(zone)
      }
      parent.exclusionZone = true
    }

    order.recalculateMonitoringStartEndDate()
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

  fun removeTagAtSource(orderId: UUID, username: String) {
    val order = this.findEditableOrder(orderId, username)

    this.clearTagAtSource(order)

    updateLastUpdatedByAndSaveOrder(order)
  }

  fun removeMonitoringType(orderId: UUID, username: String, monitoringTypeId: UUID) {
    val order = this.findEditableOrder(orderId, username)

    when {
      idMatchesCurfew(order, monitoringTypeId) -> {
        order.monitoringConditions?.curfew = false
        this.clearCurfew(order)
      }

      idMatchesTrail(order, monitoringTypeId) -> {
        order.monitoringConditions?.trail = false
        this.clearTrailData(order)
      }

      idMatchesAlcohol(order, monitoringTypeId) -> {
        order.monitoringConditions?.alcohol = false
        this.clearAlcoholData(order)
      }

      order.enforcementZoneConditions.removeIf { it.id == monitoringTypeId } -> {
        order.enforcementZoneConditions.forEachIndexed { index, zone -> zone.zoneId = index }
        if (order.enforcementZoneConditions.isEmpty()) {
          order.monitoringConditions?.exclusionZone = false
        }
      }

      order.mandatoryAttendanceConditions.removeIf { it.id == monitoringTypeId } -> {
        if (order.mandatoryAttendanceConditions.isEmpty()) {
          order.monitoringConditions?.mandatoryAttendance = false
        }
      }
    }
    order.recalculateMonitoringStartEndDate()
    updateLastUpdatedByAndSaveOrder(order)
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
