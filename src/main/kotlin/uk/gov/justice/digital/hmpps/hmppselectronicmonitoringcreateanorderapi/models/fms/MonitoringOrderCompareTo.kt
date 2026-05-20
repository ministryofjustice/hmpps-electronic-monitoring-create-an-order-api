package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.MonitoringOrderChangedMessages

fun MonitoringOrder.compareTo(previous: MonitoringOrder): List<String> {
  val messages = mutableSetOf<String>()

  fun compareField(key: String, new: Any?, old: Any?) {
    if (old != new) {
      MonitoringOrderChangedMessages.messages[key]?.let { messages += it }
    }
  }

  fun isNotNullOrEmpty(value: Any?): Boolean = value != null && value != ""

  fun compareFiledIfNewExists(key: String, new: Any?, old: Any?) {
    if (isNotNullOrEmpty(new) && old != new) {
      MonitoringOrderChangedMessages.messages[key]?.let { messages += it }
    }
  }

  fun <T> compareList(key: String, new: List<T>?, old: List<T>?) {
    if ((old ?: emptyList()) != (new ?: emptyList<T>())) {
      MonitoringOrderChangedMessages.messages[key]?.let { messages += it }
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

  compareField("conditionType", conditionType, previous.conditionType)
  compareField("offenceAdditionalDetails", offenceAdditionalDetails, previous.offenceAdditionalDetails)
  compareField("orderStart", orderStart, previous.orderStart)
  compareField("orderEnd", orderEnd, previous.orderEnd)
  compareField("orderType", orderType, previous.orderType)
  compareField("notifyingOrganization", notifyingOrganization, previous.notifyingOrganization)
  compareField("noEmail", noEmail, previous.noEmail)
  compareField("noName", noName, previous.noName)
  compareFiledIfNewExists("pduResponsible", pduResponsible, previous.pduResponsible)
  compareFiledIfNewExists("responsibleOfficerEmail", responsibleOfficerEmail, previous.responsibleOfficerEmail)
  compareFiledIfNewExists("responsibleOfficerName", responsibleOfficerName, previous.responsibleOfficerName)
  compareFiledIfNewExists("responsibleOrganization", responsibleOrganization, previous.responsibleOrganization)
  compareFiledIfNewExists("roEmail", roEmail, previous.roEmail)
  compareFiledIfNewExists("roRegion", roRegion, previous.roRegion)
  compareField("sentenceType", sentenceType, previous.sentenceType)
  compareField("tagAtSource", tagAtSource, previous.tagAtSource)
  compareField("tagAtSourceDetails", tagAtSourceDetails, previous.tagAtSourceDetails)
  compareField(
    "dateAndTimeInstallationWillTakePlace",
    dateAndTimeInstallationWillTakePlace,
    previous.dateAndTimeInstallationWillTakePlace,
  )
  compareField("curfewDescription", curfewDescription, previous.curfewDescription)
  compareField("curfewStart", curfewStart, previous.curfewStart)
  compareField("curfewEnd", curfewEnd, previous.curfewEnd)
  compareField("abstinence", abstinence, previous.abstinence)
  compareField("issp", issp, previous.issp)
  compareField("hdc", hdc, previous.hdc)
  compareField("pilot", pilot, previous.pilot)
  compareField("releasedUnderPrarr", releasedUnderPrarr, previous.releasedUnderPrarr)
  compareField("dapolMissedInError", dapolMissedInError, previous.dapolMissedInError)
  compareField("installAtSourcePilot", installAtSourcePilot, previous.installAtSourcePilot)

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
    MonitoringOrderChangedMessages.messages["installationAddress"]?.let { messages += it }
  }
  compareField(
    "crownCourtCaseReferenceNumber",
    crownCourtCaseReferenceNumber,
    previous.crownCourtCaseReferenceNumber,
  )

  compareList("exclusionZones", exclusionZones, previous.exclusionZones)
  compareList("acEligibleOffences", acEligibleOffences, previous.acEligibleOffences)
  compareList("dapoOrderClauseNumbers", dapoOrderClauseNumbers, previous.dapoOrderClauseNumbers)
  compareList("offences", offences, previous.offences)
  return messages.toList()
}
