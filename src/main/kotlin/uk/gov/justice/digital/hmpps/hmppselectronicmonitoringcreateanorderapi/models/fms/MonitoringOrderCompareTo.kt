package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import MonitoringOrderChange
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.OrderChangeDetail

fun MonitoringOrder.compareTo(previous: MonitoringOrder): MonitoringOrderCompareToResult {
  val result = MonitoringOrderCompareToResult()

  fun compareField(change: MonitoringOrderChange, new: Any?, old: Any?) {
    if (old != new) {
      result.addChange(change)
    }
  }

  fun isNotNullOrEmpty(value: Any?): Boolean = value != null && value != ""

  fun compareFiledIfNewExists(change: MonitoringOrderChange, new: Any?, old: Any?) {
    if (isNotNullOrEmpty(new) && old != new) {
      result.addChange(change)
    }
  }

  fun <T> compareList(change: MonitoringOrderChange, new: List<T>?, old: List<T>?) {
    if ((old ?: emptyList()) != (new ?: emptyList<T>())) {
      result.addChange(change)
    }
  }

  fun findVariationTypeForAddEnforceableCondition(condition: String): VariationType = when (condition) {
    "Location Monitoring (using Non-Fitted Device)" -> VariationType.CHANGE_TO_DEVICE_TYPE
    "Location Monitoring (Fitted Device)" -> VariationType.CHANGE_TO_DEVICE_TYPE
    "EM Exclusion / Inclusion Zone" -> VariationType.CHANGE_TO_ADD_AN_EXCLUSION_ZONES
    else -> VariationType.CHANGE_TO_ENFORCEABLE_CONDITION
  }

  fun findVariationTypeForEditEnforceableCondition(condition: String): VariationType = when (condition) {
    "Location Monitoring (using Non-Fitted Device)" -> VariationType.CHANGE_TO_DEVICE_TYPE
    "Location Monitoring (Fitted Device)" -> VariationType.CHANGE_TO_DEVICE_TYPE
    "EM Exclusion / Inclusion Zone" -> VariationType.CHANGE_TO_AN_EXISTING_EXCLUSION
    else -> VariationType.CHANGE_TO_ENFORCEABLE_CONDITION
  }

  fun compareEnforceableConditions(newList: List<EnforceableCondition>?, oldList: List<EnforceableCondition>?) {
    val oldByCondition = oldList.orEmpty().associateBy { it.condition }
    val newByCondition = newList.orEmpty().associateBy { it.condition }

    // Added conditions
    val addedConditions = newByCondition.keys - oldByCondition.keys
    addedConditions.forEach { condition ->
      if (!condition.isNullOrEmpty()) {
        result.addMessage("$condition has been added")
        result.addOrderVariationType(findVariationTypeForAddEnforceableCondition(condition))
      }
    }

    // Deleted conditions
    val deletedConditions = oldByCondition.keys - newByCondition.keys
    deletedConditions.forEach { condition ->
      if (!condition.isNullOrEmpty()) {
        result.addMessage("$condition has been deleted")
        result.addOrderVariationType(findVariationTypeForEditEnforceableCondition(condition))
      }
    }

    // Changed conditions
    oldByCondition.keys
      .intersect(newByCondition.keys)
      .forEach { condition ->
        if (!condition.isNullOrEmpty()) {
          val oldCond = oldByCondition[condition]!!
          val newCond = newByCondition[condition]!!

          if (oldCond.startDate != newCond.startDate) {
            result.addMessage("$condition start date has changed")
            result.addOrderVariationType(VariationType.CHANGE_TO_ENFORCEABLE_CONDITION)
          }
          if (oldCond.endDate != newCond.endDate) {
            result.addMessage("$condition end date has changed")
            result.addOrderVariationType(VariationType.CHANGE_TO_ENFORCEABLE_CONDITION)
          }
        }
      }
  }

  fun normalizeSchedule(schedule: List<Schedule>?): List<Triple<String?, String?, String?>> = schedule.orEmpty()
    .map {
      Triple(
        it.day?.lowercase(),
        it.start,
        it.end,
      )
    }
    .sortedWith(compareBy({ it.first }, { it.second }, { it.third }))

  fun compareCurfewDuration(newList: List<CurfewSchedule>?, oldList: List<CurfewSchedule>?) {
    val oldByLocation = oldList.orEmpty()
      .associateBy { it.location?.lowercase() }
    val newByLocation = newList.orEmpty()
      .associateBy { it.location?.lowercase() }

    val added = newByLocation.keys - oldByLocation.keys
    added.filterNotNull().forEach { location ->
      result.addMessage("Curfew timetable for $location address has been added")
      result.addOrderVariationType(VariationType.CHANGE_TO_CURFEW_HOURS)
    }

    val deleted = oldByLocation.keys - newByLocation.keys
    deleted.filterNotNull().forEach { location ->
      result.addMessage("Curfew timetable for $location address has been deleted")
      result.addOrderVariationType(VariationType.CHANGE_TO_CURFEW_HOURS)
    }

    oldByLocation.keys
      .intersect(newByLocation.keys)
      .filterNotNull()
      .forEach { location ->
        val oldSchedule = normalizeSchedule(oldByLocation[location]?.schedule)
        val newSchedule = normalizeSchedule(newByLocation[location]?.schedule)
        if (oldSchedule != newSchedule) {
          result.addMessage("Curfew timetable for $location address has been changed")
          result.addOrderVariationType(VariationType.CHANGE_TO_CURFEW_HOURS)
        }
      }
  }

  compareEnforceableConditions(
    this.enforceableCondition,
    previous.enforceableCondition,
  )

  compareCurfewDuration(
    this.curfewDuration,
    previous.curfewDuration,
  )

  compareField(MonitoringOrderChange.ConditionType, conditionType, previous.conditionType)
  compareField(
    MonitoringOrderChange.OffenceAdditionalDetails,
    offenceAdditionalDetails,
    previous.offenceAdditionalDetails,
  )
  compareField(MonitoringOrderChange.OrderStart, orderStart, previous.orderStart)
  compareField(MonitoringOrderChange.OrderEnd, orderEnd, previous.orderEnd)
  compareField(MonitoringOrderChange.OrderType, orderType, previous.orderType)
  compareField(MonitoringOrderChange.NotifyingOrganization, notifyingOrganization, previous.notifyingOrganization)
  compareField(MonitoringOrderChange.NoEmail, noEmail, previous.noEmail)
  compareField(MonitoringOrderChange.NoName, noName, previous.noName)
  compareFiledIfNewExists(MonitoringOrderChange.PduResponsible, pduResponsible, previous.pduResponsible)
  compareFiledIfNewExists(
    MonitoringOrderChange.ResponsibleOfficerEmail,
    responsibleOfficerEmail,
    previous.responsibleOfficerEmail,
  )
  compareFiledIfNewExists(
    MonitoringOrderChange.ResponsibleOfficerName,
    responsibleOfficerName,
    previous.responsibleOfficerName,
  )
  compareFiledIfNewExists(
    MonitoringOrderChange.ResponsibleOrganization,
    responsibleOrganization,
    previous.responsibleOrganization,
  )
  compareFiledIfNewExists(MonitoringOrderChange.RoEmail, roEmail, previous.roEmail)
  compareFiledIfNewExists(MonitoringOrderChange.RoRegion, roRegion, previous.roRegion)
  compareField(MonitoringOrderChange.SentenceType, sentenceType, previous.sentenceType)
  compareField(MonitoringOrderChange.TagAtSource, tagAtSource, previous.tagAtSource)
  compareField(MonitoringOrderChange.TagAtSourceDetails, tagAtSourceDetails, previous.tagAtSourceDetails)
  compareField(
    MonitoringOrderChange.DateAndTimeInstallationWillTakePlace,
    dateAndTimeInstallationWillTakePlace,
    previous.dateAndTimeInstallationWillTakePlace,
  )
  compareField(MonitoringOrderChange.CurfewDescription, curfewDescription, previous.curfewDescription)
  compareField(MonitoringOrderChange.CurfewStart, curfewStart, previous.curfewStart)
  compareField(MonitoringOrderChange.CurfewEnd, curfewEnd, previous.curfewEnd)
  compareField(
    MonitoringOrderChange.ConditionalReleaseStartTime,
    conditionalReleaseStartTime,
    previous.conditionalReleaseStartTime,
  )
  compareField(
    MonitoringOrderChange.ConditionalReleaseEndTime,
    conditionalReleaseEndTime,
    previous.conditionalReleaseEndTime,
  )
  compareField(MonitoringOrderChange.Abstinence, abstinence, previous.abstinence)
  compareField(MonitoringOrderChange.Issp, issp, previous.issp)
  compareField(MonitoringOrderChange.Hdc, hdc, previous.hdc)
  compareField(MonitoringOrderChange.Pilot, pilot, previous.pilot)
  compareField(MonitoringOrderChange.ReleasedUnderPrarr, releasedUnderPrarr, previous.releasedUnderPrarr)
  compareField(MonitoringOrderChange.DapolMissedInError, dapolMissedInError, previous.dapolMissedInError)
  compareField(MonitoringOrderChange.InstallAtSourcePilot, installAtSourcePilot, previous.installAtSourcePilot)

  if (listOf(
      installationAddress1,
      installationAddress2,
      installationAddress3,
      installationAddress4,
      installationAddressPostcode,
    ) != listOf(
      previous.installationAddress1,
      previous.installationAddress2,
      previous.installationAddress3,
      previous.installationAddress4,
      previous.installationAddressPostcode,
    )
  ) {
    result.addMessage(MonitoringOrderChange.InstallationAddress.message)
    result.addOrderVariationType(VariationType.CHANGE_TO_ADDRESS)
  }
  compareField(
    MonitoringOrderChange.CourtCaseReferenceNumber,
    crownCourtCaseReferenceNumber,
    previous.crownCourtCaseReferenceNumber,
  )
  compareField(
    MonitoringOrderChange.CourtCaseReferenceNumber,
    magistrateCourtCaseReferenceNumber,
    previous.magistrateCourtCaseReferenceNumber,
  )

  compareList(MonitoringOrderChange.ExclusionZones, exclusionZones, previous.exclusionZones)
  compareList(MonitoringOrderChange.AcEligibleOffences, acEligibleOffences, previous.acEligibleOffences)
  compareList(MonitoringOrderChange.DapoOrderClauseNumbers, dapoOrderClauseNumbers, previous.dapoOrderClauseNumbers)
  compareList(MonitoringOrderChange.Offences, offences, previous.offences)
  return result
}

class MonitoringOrderCompareToResult : CompareToResult<MonitoringOrderChange>()

open class CompareToResult<T : OrderChangeDetail> {
  private val _messages = mutableListOf<String>()
  val messages: MutableList<String> get() = _messages

  private val orderVariationTypes = mutableListOf<VariationType>()
  val orderVariationType: VariationType
    get() {
      return orderVariationTypes.minByOrNull { it.priority } ?: VariationType.OTHER
    }

  fun addChange(change: T) {
    _messages += change.message
    orderVariationTypes += change.orderVariationType
  }

  fun addOrderVariationType(variationType: VariationType) {
    orderVariationTypes += variationType
  }

  fun addMessage(message: String) {
    _messages += message
  }
}
