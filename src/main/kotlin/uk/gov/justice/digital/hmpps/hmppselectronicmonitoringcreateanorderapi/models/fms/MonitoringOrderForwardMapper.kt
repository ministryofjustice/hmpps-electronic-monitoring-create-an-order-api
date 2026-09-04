package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CivilCountyCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FamilyCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MagistrateCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MagistrateCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MilitaryCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.PoliceAreas
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationDeliveryUnits
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SentenceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthCustodyServiceRegionDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthJusticeServiceRegions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.PoliceAreasDDv6
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.ProbationDeliveryUnitsDDv6
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.YouthCustodyServiceRegionDDv6
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.formatters.PhoneNumberFormatter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private fun getBritishDateAndTime(dateTime: ZonedDateTime?): String? = FmsDates.getBritishDateAndTime(dateTime)

private fun getBritishDate(dateTime: ZonedDateTime?): String? = FmsDates.getBritishDate(dateTime)

fun MonitoringOrder.Companion.fromOrder(
  order: Order,
  caseId: String?,
  featureFlags: FeatureFlags,
  orderSource: FmsOrderSource,
): MonitoringOrder {
  val defaultEndDate =
    ZonedDateTime.of(2040, 1, 1, 23, 59, 0, 0, ZoneId.of("Europe/London"))
      .takeIf { orderSource == FmsOrderSource.CEMO }

  val conditions = order.monitoringConditions!!
  val monitoringStartDate = order.getMonitoringStartDate()

  val isBail = conditions.orderType === OrderType.BAIL || conditions.orderType === OrderType.IMMIGRATION
  val subcategory = RequestType.getSubCategory(order.type, isBail, monitoringStartDate)

  val monitoringEndDate: ZonedDateTime? = when (subcategory) {
    "SR11-Removal of devices (bail)" -> {
      val now = LocalDateTime.now()
      ZonedDateTime.of(now.year, now.monthValue, now.dayOfMonth, 23, 59, 0, 0, ZoneId.systemDefault())
    }

    "SR21-Revocation monitoring requirements" -> {
      if (monitoringStartDate != null &&
        monitoringStartDate.toLocalDate() < ZonedDateTime.now().toLocalDate() &&
        order.type == RequestType.END_MONITORING
      ) {
        val now = LocalDateTime.now()
        ZonedDateTime.of(now.year, now.monthValue, now.dayOfMonth, 23, 59, 0, 0, ZoneId.systemDefault())
      } else {
        val aWeekInFuture = LocalDateTime.now().plusDays(7)
        ZonedDateTime.of(
          aWeekInFuture.year,
          aWeekInFuture.monthValue,
          aWeekInFuture.dayOfMonth,
          23,
          59,
          0,
          0,
          ZoneId.systemDefault(),
        )
      }
    }

    else -> {
      order.getMonitoringEndDate() ?: defaultEndDate
    }
  }

  val monitoringOrder = MonitoringOrder(
    deviceWearer = getDeviceWearerName(order.deviceWearer!!),
    orderType = getOrderType(order, conditions.orderType!!),
    orderRequestType = order.type.value,
    orderTypeDescription = conditions.orderTypeDescription?.value ?: "",
    orderStart = getBritishDateAndTime(monitoringStartDate),
    orderEnd = getBritishDateAndTime(monitoringEndDate) ?: "",
    interimCourtDate = getBritishDateAndTime(conditions.nextCourtHearingDate) ?: "",
    serviceEndDate = getBritishDate(monitoringEndDate) ?: "",
    caseId = caseId,
    conditionType = conditions.conditionType!!.value,
    orderId = order.id.toString(),
    orderStatus = "Not Started",
    offenceAdditionalDetails = getOffenceAdditionalDetails(order, featureFlags),
    pilot = conditions.pilot?.value ?: "",
    additionalInformation = order.monitoringOrderAddtionalInfo,
  )

  if (order.interestedParties?.notifyingOrganisation == NotifyingOrganisationDDv5.MAGISTRATES_COURT.value) {
    monitoringOrder.magistrateCourtCaseReferenceNumber = order.deviceWearer?.courtCaseReferenceNumber ?: ""
  } else if (order.interestedParties?.notifyingOrganisation == NotifyingOrganisationDDv5.CROWN_COURT.value) {
    monitoringOrder.crownCourtCaseReferenceNumber = order.deviceWearer?.courtCaseReferenceNumber ?: ""
  }
  if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV6)) {
    monitoringOrder.subcategory = subcategory
    monitoringOrder.dapolMissedInError = getDapolMissedInError(order)
    if (featureFlags.ddV6CourtMappings) {
      conditions.offenceType?.takeIf { it.isNotBlank() }?.let {
        monitoringOrder.acEligibleOffences = mutableListOf(
          OffenceData(
            offence = it,
            offenceDate = "",
          ),
        )
      }
    }
  }

  monitoringOrder.sentenceType = getSentenceType(order, featureFlags)
  monitoringOrder.issp = if (conditions.issp == YesNoUnknown.YES) {
    "Yes"
  } else {
    "No"
  }
  monitoringOrder.hdc = if (conditions.hdc == YesNoUnknown.YES) {
    "Yes"
  } else {
    "No"
  }

  if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV6) &&
    featureFlags.ddV6CourtMappings
  ) {
    monitoringOrder.dapoOrderClauseNumbers?.addAll(
      order.dapoClauses.map {
        DapoClause(
          dapoOrderClauseNumber = it.clause,
          date = getBritishDate(it.date),
        )
      },
    )

    monitoringOrder.offences?.addAll(
      order.offences.map {
        OffenceData(
          offence = Offence.from(it.offenceType)?.value ?: it.offenceType,
          offenceDate = getBritishDate(it.offenceDate),
        )
      },
    )
  } else {
    monitoringOrder.offence = getOffence(order)
  }

  order.interestedParties?.let { parties ->
    val startDateIsInPast =
      monitoringStartDate != null && (monitoringStartDate.toLocalDate() < ZonedDateTime.now().toLocalDate())
    val orderIsVariation = RequestType.VARIATION_TYPES.contains(order.type)
    val cemoVariationInPast = startDateIsInPast && orderIsVariation && orderSource == FmsOrderSource.CEMO

    monitoringOrder.apply {
      responsibleOfficerName = if (cemoVariationInPast) "" else parties.getResponsibleOfficerFullName()
      responsibleOfficerPhone = if (cemoVariationInPast) "" else getResponsibleOfficerPhoneNumber(parties)
      responsibleOfficerEmail = if (cemoVariationInPast) "" else parties.responsibleOfficerEmail ?: ""
      responsibleOrganization = if (cemoVariationInPast) "" else getResponsibleOrganisation(parties)
      roRegion = if (cemoVariationInPast) "" else getResponsibleOrganisationRegion(parties)
      if (responsibleOrganization == ResponsibleOrganisation.PROBATION.value
      ) {
        pduResponsible = if (cemoVariationInPast) "" else getProbationDeliveryUnit(order)
      }
      roEmail = if (cemoVariationInPast) "" else parties.responsibleOrganisationEmail

      notifyingOrganization = getNotifyingOrganisation(parties, order.dataDictionaryVersion)
      notifyingOfficerName =
        if (notifyingOrganization == NotifyingOrganisationDDv5.HOME_OFFICE.value) "Home Office" else ""
      noName = getNotifyingOrganisationName(parties, order.dataDictionaryVersion)
      noEmail = parties.notifyingOrganisationEmail

      if (featureFlags.ddV6CourtMappings) {
        val parties = order.interestedParties
        val orgValue = parties?.notifyingOrganisation

        val matchingEnum = NotifyingOrganisationDDv5.from(orgValue)
          ?: NotifyingOrganisationDDv5.entries.find { it.value == orgValue }

        val isCourt = matchingEnum != null && NotifyingOrganisationDDv5.COURTS.contains(matchingEnum)

        responsibleOfficerDetailsReceived = if (isCourt || cemoVariationInPast) {
          "No"
        } else {
          "Yes"
        }
      }

      if (cemoVariationInPast) {
        responsibleOfficerName = ""
        responsibleOfficerPhone = ""
        responsibleOfficerEmail = ""
        responsibleOrganization = ""
        roRegion = ""
        roEmail = ""
      }
    }
  }

  if (order.curfewConditions?.startDate != null) {
    val curfew = order.curfewConditions!!
    monitoringOrder.enforceableCondition!!.add(
      EnforceableCondition(
        "Curfew with EM",
        startDate = getBritishDateAndTime(curfew.startDate),
        endDate = getBritishDateAndTime(curfew.endDate ?: defaultEndDate) ?: "",
      ),
    )
    monitoringOrder.curfewDescription = curfew.curfewAdditionalDetails ?: ""
    monitoringOrder.conditionalReleaseDate = getBritishDate(order.curfewReleaseDateConditions?.releaseDate)
    monitoringOrder.conditionalReleaseStartTime = order.curfewReleaseDateConditions?.startTime ?: ""
    monitoringOrder.conditionalReleaseEndTime = order.curfewReleaseDateConditions?.endTime ?: ""
    monitoringOrder.curfewStart = getBritishDateAndTime(curfew.startDate)
    monitoringOrder.curfewEnd = getBritishDateAndTime(curfew.endDate ?: defaultEndDate) ?: ""
    monitoringOrder.curfewDuration = getCurfewSchedules(order)
  }

  if (order.monitoringConditionsTrail?.startDate != null) {
    val deviceType =
      if (order.monitoringConditionsTrail!!.deviceType ==
        DeviceType.NON_FITTED
      ) {
        "Location Monitoring (using Non-Fitted Device)"
      } else {
        "Location Monitoring (Fitted Device)"
      }

    monitoringOrder.enforceableCondition!!.add(
      EnforceableCondition(
        deviceType,
        startDate = getBritishDateAndTime(order.monitoringConditionsTrail!!.startDate),
        endDate = getBritishDateAndTime(order.monitoringConditionsTrail!!.endDate ?: defaultEndDate) ?: "",
      ),

    )
    monitoringOrder.trailMonitoring = "Yes"
  }

  if (order.enforcementZoneConditions.count() > 0) {
    val enforcementZoneStartDate = order.enforcementZoneConditions.mapNotNull { it.startDate }.minOrNull()
    val enforcementZoneEndDate =
      order.enforcementZoneConditions.mapNotNull { it.endDate }.maxOrNull() ?: defaultEndDate

    if (order.enforcementZoneConditions.any {
        it.zoneType in setOf(
          EnforcementZoneType.INCLUSION,
          EnforcementZoneType.EXCLUSION,
        )
      }
    ) {
      monitoringOrder.enforceableCondition?.add(
        EnforceableCondition(
          "EM Exclusion / Inclusion Zone",
          startDate = getBritishDateAndTime(enforcementZoneStartDate),
          endDate = getBritishDateAndTime(enforcementZoneEndDate) ?: "",
        ),
      )
    }

    if (order.enforcementZoneConditions.any { it.zoneType == EnforcementZoneType.RESTRICTION }) {
      monitoringOrder.enforceableCondition?.add(
        EnforceableCondition(
          "Restriction Zones",
          startDate = getBritishDateAndTime(enforcementZoneStartDate),
          endDate = getBritishDateAndTime(enforcementZoneEndDate) ?: "",
        ),
      )
    }

    order.enforcementZoneConditions.forEach {
      val zone = Zone(
        description = it.description,
        duration = it.duration,
        start = getBritishDate(it.startDate),
        end = getBritishDate(it.endDate ?: defaultEndDate) ?: "",
      )

      when (it.zoneType) {
        EnforcementZoneType.EXCLUSION -> monitoringOrder.exclusionZones.add(zone)
        EnforcementZoneType.INCLUSION -> monitoringOrder.inclusionZones.add(zone)
        EnforcementZoneType.RESTRICTION -> monitoringOrder.restrictionZones.add(zone)
        else -> {}
      }
    }

    monitoringOrder.trailMonitoring = "No"
  }

  if (order.mandatoryAttendanceConditions.count() > 0) {
    val mandatoryAttendanceStartDate = order.mandatoryAttendanceConditions.mapNotNull { it.startDate }.minOrNull()
    val mandatoryAttendanceEndDate = order.mandatoryAttendanceConditions.mapNotNull { it.endDate }.maxOrNull()

    monitoringOrder.enforceableCondition!!.add(
      EnforceableCondition(
        "Attendance Requirement",
        startDate = getBritishDateAndTime(mandatoryAttendanceStartDate) ?: "",
        endDate = getBritishDateAndTime(mandatoryAttendanceEndDate) ?: "",
      ),
    )
    monitoringOrder.inclusionZones.addAll(getInclusionZones(order))
  }

  if (order.monitoringConditionsAlcohol?.startDate != null) {
    val condition = order.monitoringConditionsAlcohol!!
    var conditionType = "AAMR"
    if (monitoringOrder.notifyingOrganization == NotifyingOrganisationDDv5.PRISON.value ||
      monitoringOrder.notifyingOrganization == NotifyingOrganisationDDv5.PROBATION.value
    ) {
      conditionType = "AML"
    }

    monitoringOrder.enforceableCondition!!.add(
      EnforceableCondition(
        conditionType,
        startDate = getBritishDateAndTime(condition.startDate),
        endDate = getBritishDateAndTime(condition.endDate) ?: "",
      ),
    )
    if (condition.monitoringType == AlcoholMonitoringType.ALCOHOL_ABSTINENCE) {
      monitoringOrder.abstinence = "Yes"
    } else {
      monitoringOrder.abstinence = "No"
    }
  }

  if (conditions.prarr == YesNoUnknown.YES) {
    monitoringOrder.releasedUnderPrarr = "true"
  } else {
    monitoringOrder.releasedUnderPrarr = "false"
  }

  if (order.installationLocation != null) {
    if (order.installationLocation?.location == InstallationLocationType.PROBATION_OFFICE ||
      order.installationLocation?.location == InstallationLocationType.PRISON ||
      order.installationLocation?.location == InstallationLocationType.IMMIGRATION_REMOVAL_CENTRE
    ) {
      monitoringOrder.tagAtSource = "true"
      monitoringOrder.tagAtSourceDetails = order.installationAppointment?.placeName ?: ""
      monitoringOrder.dateAndTimeInstallationWillTakePlace =
        getBritishDateAndTime(order.installationAppointment?.appointmentDate) ?: ""
    } else if (
      (
        order.interestedParties?.notifyingOrganisation == NotifyingOrganisation.HOME_OFFICE.name &&
          order.installationLocation?.location == InstallationLocationType.PRIMARY
        ) ||
      order.installationLocation?.location == InstallationLocationType.INSTALLATION
    ) {
      monitoringOrder.tagAtSource = "false"
      monitoringOrder.tagAtSourceDetails = order.installationAppointment?.placeName ?: ""
      monitoringOrder.dateAndTimeInstallationWillTakePlace =
        getBritishDateAndTime(order.installationAppointment?.appointmentDate) ?: ""
    } else {
      monitoringOrder.tagAtSource = "false"
    }

    if (!order.installationAppointment?.appointmentTimeDetails.isNullOrEmpty()) {
      monitoringOrder.tagAtSourceDetails += " ${order.installationAppointment!!.appointmentTimeDetails}"
    }

    if (order.installationLocation!!.location != InstallationLocationType.INSTALLATION_ALREADY_TAKEN_PLACE) {
      getInstallationAddress(order)?.let {
        monitoringOrder.installationAddress1 = it.addressLine1
        monitoringOrder.installationAddress2 = it.addressLine2
        monitoringOrder.installationAddress3 = it.addressLine3
        monitoringOrder.installationAddress4 = it.addressLine4
        monitoringOrder.installationAddressPostcode = it.postcode
      }
    }
  }

  if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV6) &&
    featureFlags.ddV6CourtMappings
  ) {
    if (order.installationLocation != null) {
      if (order.installationLocation?.location == InstallationLocationType.PRISON &&
        Prison.PRISONS_IN_PILOT.any { it.name == order.interestedParties?.notifyingOrganisationName }
      ) {
        monitoringOrder.installAtSourcePilot = "true"
        monitoringOrder.tagAtSource = "false"
      } else {
        monitoringOrder.installAtSourcePilot = "false"
      }
    }
  }

  if (RequestType.VARIATION_TYPES.contains(order.type)) {
    monitoringOrder.orderVariationDate = order.variationDetails!!.variationDate.format(FmsDates.dateTimeFormatter)
    monitoringOrder.orderVariationDetails = order.variationDetails!!.variationDetails
  }

  return monitoringOrder
}

private fun getInstallationAddress(order: Order): Address? = when (order.installationLocation?.location) {
  InstallationLocationType.PRIMARY -> order.addresses.firstOrNull { it.addressType == AddressType.PRIMARY }
  InstallationLocationType.SECONDARY -> order.addresses.firstOrNull { it.addressType == AddressType.SECONDARY }
  InstallationLocationType.TERTIARY -> order.addresses.firstOrNull { it.addressType == AddressType.TERTIARY }
  else -> order.addresses.firstOrNull { it.addressType == AddressType.INSTALLATION }
}

private fun getSentenceType(order: Order, featureFlags: FeatureFlags): String {
  val sentenceType = order.monitoringConditions?.sentenceType ?: return ""

  if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV6) &&
    featureFlags.ddV6CourtMappings
  ) {
    return when (sentenceType) {
      // ELM-4515 update mapping for ddv6
      SentenceType.IPP -> "IPP (Imprisonment for Public Protection)"
      else -> sentenceType.value
    }
  }
  return sentenceType.value
}

private fun getCurfewSchedules(order: Order): MutableList<CurfewSchedule> {
  val schedules = mutableListOf<CurfewSchedule>()
  val primaryAddressTimeTable = order.curfewTimeTable.filter {
    it.curfewAddress!!.uppercase().contains("PRIMARY_ADDRESS")
  }
  if (primaryAddressTimeTable.any()) {
    schedules.add(
      CurfewSchedule(
        location = "primary",
        allday = "",
        primaryAddressTimeTable.map { Schedule.fromCurfewTimeTable(it) }.toMutableList(),
      ),
    )
  }

  val secondaryAddress = order.addresses.firstOrNull { it.addressType === AddressType.SECONDARY }
  if (secondaryAddress != null) {
    val secondaryTimeTable = order.curfewTimeTable.filter {
      it.curfewAddress!!.uppercase().contains("SECONDARY_ADDRESS")
    }
    schedules.add(
      CurfewSchedule(
        location = "secondary",
        allday = "",
        secondaryTimeTable.map { Schedule.fromCurfewTimeTable(it) }.toMutableList(),
      ),
    )
  }

  val tertiaryAddress = order.addresses.firstOrNull { it.addressType === AddressType.TERTIARY }
  if (tertiaryAddress != null) {
    val tertiaryTimeTable = order.curfewTimeTable.filter {
      it.curfewAddress!!.uppercase().contains("TERTIARY_ADDRESS")
    }
    schedules.add(
      CurfewSchedule(
        location = "tertiary",
        allday = "",
        tertiaryTimeTable.map { Schedule.fromCurfewTimeTable(it) }.toMutableList(),
      ),
    )
  }
  return schedules
}

private fun getNotifyingOrganisation(
  interestedParties: InterestedParties,
  dataDictionaryVersion: DataDictionaryVersion,
): String {
  val notifyingOrganisation = interestedParties.notifyingOrganisation
  val resolvedNotifyingOrganisation =
    when (dataDictionaryVersion) {
      DataDictionaryVersion.DDV4 -> NotifyingOrganisation.from(notifyingOrganisation)?.value
      else -> NotifyingOrganisationDDv5.from(notifyingOrganisation)?.value
    }

  return resolvedNotifyingOrganisation ?: notifyingOrganisation ?: "N/A"
}

private fun getNotifyingOrganisationName(
  interestedParties: InterestedParties,
  dataDictionaryVersion: DataDictionaryVersion,
): String {
  if (dataDictionaryVersion === DataDictionaryVersion.DDV4) {
    return Prison.from(interestedParties.notifyingOrganisationName)?.name
      ?: CrownCourt.from(interestedParties.notifyingOrganisationName)?.value
      ?: MagistrateCourt.from(interestedParties.notifyingOrganisationName)?.value
      ?: interestedParties.notifyingOrganisationName
      ?: ""
  }
  val notifyOrg = NotifyingOrganisationDDv5.from(interestedParties.notifyingOrganisation)

  if (notifyOrg == NotifyingOrganisationDDv5.PROBATION) {
    return "Probation Board"
  }

  if (notifyOrg == NotifyingOrganisationDDv5.HOME_OFFICE) {
    return NotifyingOrganisationDDv5.HOME_OFFICE.value
  }

  return CivilCountyCourtDDv5.from(interestedParties.notifyingOrganisationName)?.value
    ?: CrownCourtDDv5.from(interestedParties.notifyingOrganisationName)?.value
    ?: FamilyCourtDDv5.from(interestedParties.notifyingOrganisationName)?.value
    ?: MagistrateCourtDDv5.from(interestedParties.notifyingOrganisationName)?.value
    ?: MilitaryCourtDDv5.from(interestedParties.notifyingOrganisationName)?.value
    ?: Prison.from(interestedParties.notifyingOrganisationName)?.value
    ?: YouthCourtDDv5.from(interestedParties.notifyingOrganisationName)?.value
    ?: YouthCustodyServiceRegionDDv5.from(interestedParties.notifyingOrganisationName)?.value
    ?: YouthCustodyServiceRegionDDv6.from(interestedParties.notifyingOrganisationName)?.value
    ?: interestedParties.notifyingOrganisationName
    ?: ""
}

private fun getResponsibleOrganisation(interestedParties: InterestedParties): String =
  ResponsibleOrganisation.from(interestedParties.responsibleOrganisation)?.value
    ?: interestedParties.responsibleOrganisation?.takeIf { it.isNotBlank() }
    ?: "".takeIf { interestedParties.notifyingOrganisation != NotifyingOrganisation.HOME_OFFICE.name }
    ?: ResponsibleOrganisation.HOME_OFFICE.value

private fun getResponsibleOrganisationRegion(interestedParties: InterestedParties): String {
  if (NotifyingOrganisationDDv5.from(interestedParties.notifyingOrganisation) ==
    NotifyingOrganisationDDv5.HOME_OFFICE
  ) {
    return "UKBA"
  }

  val responsibleOrg = ResponsibleOrganisation.from(interestedParties.responsibleOrganisation)

  val rawRegion = interestedParties.responsibleOrganisationRegion

  return when (responsibleOrg) {
    ResponsibleOrganisation.PROBATION ->
      ProbationServiceRegion.from(rawRegion)?.value ?: rawRegion

    ResponsibleOrganisation.YJS ->
      YouthJusticeServiceRegions.from(rawRegion)?.value ?: rawRegion

    ResponsibleOrganisation.POLICE ->
      PoliceAreasDDv6.from(rawRegion)?.value ?: PoliceAreas.from(rawRegion)?.value ?: rawRegion

    else -> rawRegion
  } ?: ""
}

private fun getProbationDeliveryUnit(order: Order): String {
  if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV6)) {
    return ProbationDeliveryUnitsDDv6.from(order.probationDeliveryUnit?.unit)?.value ?: ""
  }
  return ProbationDeliveryUnits.from(order.probationDeliveryUnit?.unit)?.value ?: ""
}

private fun getOffence(order: Order): String =
  Offence.from(order.installationAndRisk?.offence)?.value ?: order.installationAndRisk?.offence ?: ""

private fun getResponsibleOfficerPhoneNumber(interestedParties: InterestedParties): String? {
  if (interestedParties.responsibleOfficerPhoneNumber == null) {
    return null
  }
  return PhoneNumberFormatter.formatAsInternationalDirectDialingNumber(
    interestedParties.responsibleOfficerPhoneNumber.toString(),
  )
}

private fun getInclusionZones(order: Order): List<Zone> = order.mandatoryAttendanceConditions.map {
  Zone(
    description = it.purpose + "\n" +
      it.appointmentDay + " " + it.startTime + "-" + it.endTime + "\n" +
      it.addressLine1 + "\n" +
      it.addressLine2 + "\n" +
      it.addressLine3 + "\n" +
      it.addressLine4 + "\n" +
      it.postcode + "\n",
    duration = "",
    start = getBritishDate(it.startDate),
    end = getBritishDate(it.endDate),
  )
}

private fun getOffenceAdditionalDetails(order: Order, featureFlags: FeatureFlags): String {
  val riskOffenceDetails =
    if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV6)) {
      order.offenceAdditionalDetails?.additionalDetails ?: ""
    } else {
      order.installationAndRisk?.offenceAdditionalDetails ?: ""
    }

  val monitoringOffenceType = order.monitoringConditions?.offenceType ?: ""
  var monitoringPoliceArea = PoliceAreas.from(order.monitoringConditions?.policeArea)?.value

  if (monitoringPoliceArea == null) {
    monitoringPoliceArea = order.monitoringConditions?.policeArea ?: ""
  }

  val parts = listOfNotNull(
    riskOffenceDetails.takeIf { it.isNotBlank() },
    if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV6) &&
      featureFlags.ddV6CourtMappings
    ) {
      null
    } else {
      monitoringOffenceType.takeIf { it.isNotBlank() }?.let { "AC Offence: $it" }
    },
    monitoringPoliceArea.takeIf { it.isNotBlank() }
      ?.let { "PFA: $it" },
  )

  return parts.joinToString(". ")
}

private fun getDapolMissedInError(order: Order): String {
  val conditions = order.monitoringConditions ?: return ""

  val notifyingOrg = order.interestedParties?.notifyingOrganisation
  if (notifyingOrg != NotifyingOrganisationDDv5.PROBATION.name) {
    return ""
  }

  val isDapolPilot = conditions.pilot == Pilot.DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_DAPOL ||
    conditions.pilot == Pilot.DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_HOME_DETENTION_CURFEW_DAPOL_HDC

  if (!isDapolPilot) {
    return ""
  }

  return if (conditions.dapolMissedInError == YesNoUnknown.YES) "true" else ""
}

private fun getOrderType(order: Order, orderType: OrderType): String {
  val notifyingOrg = order.interestedParties?.notifyingOrganisation

  if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV7) &&
    (
      notifyingOrg == NotifyingOrganisationDDv5.CIVIL_COUNTY_COURT.name ||
        notifyingOrg == NotifyingOrganisationDDv5.FAMILY_COURT.name
      )
  ) {
    return OrderType.CIVIL.value
  }

  return when (orderType) {
    OrderType.CIVIL, OrderType.BAIL -> OrderType.PRE_TRIAL.value
    else -> orderType.value
  }
}

private fun getDeviceWearerName(deviceWearer: DeviceWearer): String = with(deviceWearer) {
  listOfNotNull(firstName, middleName, lastName)
    .filterNot { it.isBlank() }
    .joinToString(" ")
}
