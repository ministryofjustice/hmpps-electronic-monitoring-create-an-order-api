package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.MonitoringOrderChange

fun MonitoringOrder.compareTo(previous: MonitoringOrder): List<String> {
  val messages = mutableSetOf<String>()

  fun compareField(change: MonitoringOrderChange, new: Any?, old: Any?) {
    if (old != new) {
      messages += change.message
    }
  }

  fun isNotNullOrEmpty(value: Any?): Boolean = value != null && value != ""

  fun compareFiledIfNewExists(change: MonitoringOrderChange, new: Any?, old: Any?) {
    if (isNotNullOrEmpty(new) && old != new) {
      messages += change.message
    }
  }

  fun <T> compareList(change: MonitoringOrderChange, new: List<T>?, old: List<T>?) {
    if ((old ?: emptyList()) != (new ?: emptyList<T>())) {
      messages += change.message
    }
  }

  fun compareEnforceableConditions(
    newList: List<EnforceableCondition>?,
    oldList: List<EnforceableCondition>?,
  ): List<String> {
    val messages = mutableListOf<String>()

    val oldByCondition = oldList.orEmpty().associateBy { it.condition }
    val newByCondition = newList.orEmpty().associateBy { it.condition }

    // Added conditions
    val addedConditions = newByCondition.keys - oldByCondition.keys
    addedConditions.forEach { condition ->
      messages += "$condition has been added"
    }

    // Deleted conditions
    val deletedConditions = oldByCondition.keys - newByCondition.keys
    deletedConditions.forEach { condition ->
      messages += "$condition has been deleted"
    }

    // Changed conditions
    oldByCondition.keys
      .intersect(newByCondition.keys)
      .forEach { condition ->
        val oldCond = oldByCondition[condition]!!
        val newCond = newByCondition[condition]!!
        if (oldCond.startDate != newCond.startDate) {
          messages += "$condition start date has changed"
        }
        if (oldCond.endDate != newCond.endDate) {
          messages += "$condition end date has changed"
        }
      }
    return messages
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

  fun compareCurfewDuration(newList: List<CurfewSchedule>?, oldList: List<CurfewSchedule>?): List<String> {
    val messages = mutableListOf<String>()

    val oldByLocation = oldList.orEmpty()
      .associateBy { it.location?.lowercase() }
    val newByLocation = newList.orEmpty()
      .associateBy { it.location?.lowercase() }

    val added = newByLocation.keys - oldByLocation.keys
    added.filterNotNull().forEach { location ->
      messages += "Curfew timetable for $location address has been added"
    }

    val deleted = oldByLocation.keys - newByLocation.keys
    deleted.filterNotNull().forEach { location ->
      messages += "Curfew timetable for $location address has been deleted"
    }

    oldByLocation.keys
      .intersect(newByLocation.keys)
      .filterNotNull()
      .forEach { location ->
        val oldSchedule = normalizeSchedule(oldByLocation[location]?.schedule)
        val newSchedule = normalizeSchedule(newByLocation[location]?.schedule)
        if (oldSchedule != newSchedule) {
          messages += "Curfew timetable for $location address has been changed"
        }
      }

    return messages
  }

  messages += compareEnforceableConditions(
    this.enforceableCondition,
    previous.enforceableCondition,
  )

  messages += compareCurfewDuration(
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
    messages += MonitoringOrderChange.InstallationAddress.message
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
  return messages.toList()
}
