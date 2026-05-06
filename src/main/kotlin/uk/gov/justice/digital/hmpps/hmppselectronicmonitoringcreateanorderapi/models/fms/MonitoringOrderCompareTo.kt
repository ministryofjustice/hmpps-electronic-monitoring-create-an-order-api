package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.MonitoringOrderChangedMessages
fun MonitoringOrder.compareTo(updated: MonitoringOrder): List<String> {
  val messages = mutableSetOf<String>()

  fun compareField(key: String, old: Any?, new: Any?) {
    if (old != new) {
      MonitoringOrderChangedMessages.messages[key]?.let { messages += it }
    }
  }

  fun <T> compareList(key: String, old: List<T>?, new: List<T>?) {
    if ((old ?: emptyList()) != (new ?: emptyList<T>())) {
      MonitoringOrderChangedMessages.messages[key]?.let { messages += it }
    }
  }

  fun compareEnforceableConditions(
    oldList: List<EnforceableCondition>?,
    newList: List<EnforceableCondition>?,
  ): List<String> {
    val messages = mutableListOf<String>()

    val oldByCondition = oldList.orEmpty().associateBy { it.condition }
    val newByCondition = newList.orEmpty().associateBy { it.condition }

    // ✅ Added conditions
    val addedConditions = newByCondition.keys - oldByCondition.keys
    addedConditions.forEach { condition ->
      messages += "$condition has been added"
    }

    // ✅ Deleted conditions
    val deletedConditions = oldByCondition.keys - newByCondition.keys
    deletedConditions.forEach { condition ->
      messages += "$condition has been deleted"
    }

    // ✅ Changed conditions
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

  fun compareCurfewDuration(oldList: List<CurfewSchedule>?, newList: List<CurfewSchedule>?): List<String> {
    val messages = mutableListOf<String>()

    val oldByLocation = oldList.orEmpty()
      .associateBy { it.location?.lowercase() }

    val newByLocation = newList.orEmpty()
      .associateBy { it.location?.lowercase() }

    // ✅ Added
    val added = newByLocation.keys - oldByLocation.keys
    added.filterNotNull().forEach { location ->
      messages += "Curfew timetable location $location has been added"
    }

    // ✅ Deleted
    val deleted = oldByLocation.keys - newByLocation.keys
    deleted.filterNotNull().forEach { location ->
      messages += "Curfew timetable location $location has been deleted"
    }

    // ✅ Changed (schedule comparison)
    oldByLocation.keys
      .intersect(newByLocation.keys)
      .filterNotNull()
      .forEach { location ->

        val oldSchedule = normalizeSchedule(oldByLocation[location]?.schedule)
        val newSchedule = normalizeSchedule(newByLocation[location]?.schedule)

        if (oldSchedule != newSchedule) {
          messages += "Curfew timetable location $location has been changed"
        }
      }

    return messages
  }

  messages += compareEnforceableConditions(
    this.enforceableCondition,
    updated.enforceableCondition,
  )

  messages += compareCurfewDuration(
    this.curfewDuration,
    updated.curfewDuration,
  )

  // ✅ Scalar fields
  compareField("conditionType", conditionType, updated.conditionType)
  compareField("offenceAdditionalDetails", offenceAdditionalDetails, updated.offenceAdditionalDetails)
  compareField("orderStart", orderStart, updated.orderStart)
  compareField("orderEnd", orderEnd, updated.orderEnd)
  compareField("orderType", orderType, updated.orderType)

  compareField("notifyingOrganization", notifyingOrganization, updated.notifyingOrganization)
  compareField("noEmail", noEmail, updated.noEmail)
  compareField("noName", noName, updated.noName)
  compareField("pduResponsible", pduResponsible, updated.pduResponsible)

  compareField("responsibleOfficerEmail", responsibleOfficerEmail, updated.responsibleOfficerEmail)
  compareField("responsibleOfficerName", responsibleOfficerName, updated.responsibleOfficerName)
  compareField("responsibleOrganization", responsibleOrganization, updated.responsibleOrganization)
  compareField("roEmail", roEmail, updated.roEmail)
  compareField("roRegion", roRegion, updated.roRegion)

  compareField("sentenceType", sentenceType, updated.sentenceType)
  compareField("tagAtSource", tagAtSource, updated.tagAtSource)
  compareField("tagAtSourceDetails", tagAtSourceDetails, updated.tagAtSourceDetails)
  compareField(
    "dateAndTimeInstallationWillTakePlace",
    dateAndTimeInstallationWillTakePlace,
    updated.dateAndTimeInstallationWillTakePlace,
  )

  compareField("curfewDescription", curfewDescription, updated.curfewDescription)
  compareField("curfewStart", curfewStart, updated.curfewStart)
  compareField("curfewEnd", curfewEnd, updated.curfewEnd)

  compareField("trailMonitoring", trailMonitoring, updated.trailMonitoring)
  compareField("abstinence", abstinence, updated.abstinence)
  compareField("issp", issp, updated.issp)
  compareField("hdc", hdc, updated.hdc)
  compareField("pilot", pilot, updated.pilot)
  compareField("releasedUnderPrarr", releasedUnderPrarr, updated.releasedUnderPrarr)
  compareField("dapolMissedInError", dapolMissedInError, updated.dapolMissedInError)
  compareField("installAtSourcePilot", installAtSourcePilot, updated.installAtSourcePilot)

  // Installation address treated as a group
  if (listOf(
      installationAddress1,
      installationAddress2,
      installationAddress3,
      installationAddress4,
      installationAddressPostcode,
    ) != listOf(
      updated.installationAddress1,
      updated.installationAddress2,
      updated.installationAddress3,
      updated.installationAddress4,
      updated.installationAddressPostcode,
    )
  ) {
    MonitoringOrderChangedMessages.messages["installationAddress"]?.let { messages += it }
  }

  compareField(
    "crownCourtCaseReferenceNumber",
    crownCourtCaseReferenceNumber,
    updated.crownCourtCaseReferenceNumber,
  )

  // ✅ List / complex fields
  compareList("exclusionZones", exclusionZones, updated.exclusionZones)
  compareList("acEligibleOffences", acEligibleOffences, updated.acEligibleOffences)
  compareList("dapoOrderClauseNumbers", dapoOrderClauseNumbers, updated.dapoOrderClauseNumbers)
  compareList("offences", offences, updated.offences)

  return messages.toList()
}
