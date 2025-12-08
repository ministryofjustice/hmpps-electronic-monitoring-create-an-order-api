package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CivilCountyCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FamilyCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MagistrateCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MagistrateCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MilitaryCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.PoliceAreas
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.PrisonDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationDeliveryUnits
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthCustodyServiceRegionDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthJusticeServiceRegions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.ProbationDeliveryUnitsDDv6
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.formatters.PhoneNumberFormatter
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class MonitoringOrder(
  @JsonProperty("case_id")
  var caseId: String? = "",
  @JsonProperty("allday_lockdown")
  var alldayLockdown: String? = "",
  @JsonProperty("atv_allowance")
  var atvAllowance: String? = "",
  @JsonProperty("condition_type")
  var conditionType: String? = "",
  @JsonProperty("court")
  var court: String? = "",
  @JsonProperty("court_order_email")
  var courtOrderEmail: String? = "",
  @JsonProperty("device_type")
  var deviceType: String? = "",
  @JsonProperty("device_wearer")
  var deviceWearer: String? = "",
  @JsonProperty("enforceable_condition")
  var enforceableCondition: MutableList<EnforceableCondition>? = mutableListOf(),
  @JsonProperty("exclusion_allday")
  var exclusionAllday: String? = "",
  @JsonProperty("interim_court_date")
  var interimCourtDate: String? = "",
  @JsonProperty("issuing_organisation")
  var issuingOrganisation: String? = "",
  @JsonProperty("media_interest")
  var mediaInterest: String? = "",
  @JsonProperty("new_order_received")
  var newOrderReceived: String? = "",
  @JsonProperty("notifying_officer_email")
  var notifyingOfficerEmail: String? = "",
  @JsonProperty("notifying_officer_name")
  var notifyingOfficerName: String? = "",
  @JsonProperty("notifying_organization")
  var notifyingOrganization: String? = "",
  @JsonProperty("no_post_code")
  var noPostCode: String? = "",
  @JsonProperty("no_address_1")
  var noAddress1: String? = "",
  @JsonProperty("no_address_2")
  var noAddress2: String? = "",
  @JsonProperty("no_address_3")
  var noAddress3: String? = "",
  @JsonProperty("no_address_4")
  var noAddress4: String? = "",
  @JsonProperty("no_email")
  var noEmail: String? = "",
  @JsonProperty("no_name")
  var noName: String? = "",
  @JsonProperty("no_phone_number")
  var noPhoneNumber: String? = "",
  @JsonProperty("offence")
  var offence: String? = "",
  @JsonProperty("offence_additional_details")
  val offenceAdditionalDetails: String? = "",
  @JsonProperty("offence_date")
  var offenceDate: String? = "",
  @JsonProperty("order_end")
  var orderEnd: String? = "",
  @JsonProperty("order_id")
  var orderId: String? = "",
  @JsonProperty("order_request_type")
  var orderRequestType: String? = "",
  @JsonProperty("order_start")
  var orderStart: String? = "",
  @JsonProperty("order_type")
  var orderType: String? = "",
  @JsonProperty("order_type_description")
  var orderTypeDescription: String? = "",
  @JsonProperty("order_type_detail")
  var orderTypeDetail: String? = "",
  @JsonProperty("order_variation_date")
  var orderVariationDate: String? = "",
  @JsonProperty("order_variation_details")
  var orderVariationDetails: String? = "",
  @JsonProperty("order_variation_req_received_date")
  var orderVariationReqReceivedDate: String? = "",
  @JsonProperty("order_variation_type")
  var orderVariationType: String? = "",
  @JsonProperty("pdu_responsible")
  var pduResponsible: String? = "",
  @JsonProperty("pdu_responsible_email")
  var pduResponsibleEmail: String? = "",
  @JsonProperty("planned_order_end_date")
  var plannedOrderEndDate: String? = "",
  @JsonProperty("responsible_officer_details_received")
  var responsibleOfficerDetailsReceived: String? = "",
  @JsonProperty("responsible_officer_email")
  var responsibleOfficerEmail: String? = "",
  @JsonProperty("responsible_officer_phone")
  var responsibleOfficerPhone: String? = "",
  @JsonProperty("responsible_officer_name")
  var responsibleOfficerName: String? = "",
  @JsonProperty("responsible_organization")
  var responsibleOrganization: String? = "",
  @JsonProperty("ro_post_code")
  var roPostCode: String? = "",
  @JsonProperty("ro_address_1")
  var roAddress1: String? = "",
  @JsonProperty("ro_address_2")
  var roAddress2: String? = "",
  @JsonProperty("ro_address_3")
  var roAddress3: String? = "",
  @JsonProperty("ro_address_4")
  var roAddress4: String? = "",
  @JsonProperty("ro_email")
  var roEmail: String? = "",
  @JsonProperty("ro_phone")
  var roPhone: String? = "",
  @JsonProperty("ro_region")
  var roRegion: String? = "",
  @JsonProperty("sentence_date")
  var sentenceDate: String? = "",
  @JsonProperty("sentence_expiry")
  var sentenceExpiry: String? = "",
  @JsonProperty("sentence_type")
  var sentenceType: String? = "",
  @JsonProperty("tag_at_source")
  var tagAtSource: String? = "",
  @JsonProperty("tag_at_source_details")
  var tagAtSourceDetails: String? = "",
  @JsonProperty("date_and_time_installation_will_take_place")
  var dateAndTimeInstallationWillTakePlace: String? = "",
  @JsonProperty("released_under_prarr")
  var releasedUnderPrarr: String? = "",
  @JsonProperty("technical_bail")
  var technicalBail: String? = "",
  @JsonProperty("trial_date")
  var trialDate: String? = "",
  @JsonProperty("trial_outcome")
  var trialOutcome: String? = "",
  @JsonProperty("conditional_release_date")
  var conditionalReleaseDate: String? = "",
  @JsonProperty("conditional_release_start_time")
  var conditionalReleaseStartTime: String? = "",
  @JsonProperty("conditional_release_end_time")
  var conditionalReleaseEndTime: String? = "",
  @JsonProperty("reason_for_order_ending_early")
  var reasonForOrderEndingEarly: String? = "",
  @JsonProperty("business_unit")
  var businessUnit: String? = "",
  @JsonProperty("service_end_date")
  var serviceEndDate: String? = "",
  @JsonProperty("curfew_description")
  var curfewDescription: String? = "",
  @JsonProperty("curfew_start")
  var curfewStart: String? = "",
  @JsonProperty("curfew_end")
  var curfewEnd: String? = "",
  @JsonProperty("curfew_duration")
  var curfewDuration: MutableList<CurfewSchedule>? = mutableListOf(),
  @JsonProperty("trail_monitoring")
  var trailMonitoring: String? = "",
  @JsonProperty("exclusion_zones")
  var exclusionZones: MutableList<Zone> = mutableListOf(),
  @JsonProperty("inclusion_zones")
  var inclusionZones: MutableList<Zone> = mutableListOf(),
  @JsonProperty("abstinence")
  var abstinence: String? = "",
  @JsonProperty("schedule")
  var schedule: String? = "",
  @JsonProperty("checkin_schedule")
  var checkinSchedule: MutableList<Schedule>? = mutableListOf(),
  @JsonProperty("revocation_date")
  var revocationDate: String? = "",
  @JsonProperty("revocation_type")
  var revocationType: String? = "",
  @JsonProperty("installation_address_1")
  var installationAddress1: String? = "",
  @JsonProperty("installation_address_2")
  var installationAddress2: String? = "",
  @JsonProperty("installation_address_3")
  var installationAddress3: String? = "",
  @JsonProperty("installation_address_4")
  var installationAddress4: String? = "",
  @JsonProperty("installation_address_post_code")
  var installationAddressPostcode: String? = "",
  @JsonProperty("crown_court_case_reference_number")
  var crownCourtCaseReferenceNumber: String? = "",
  @JsonProperty("magistrate_court_case_reference_number")
  var magistrateCourtCaseReferenceNumber: String? = "",
  @JsonProperty("issp")
  var issp: String = "",
  @JsonProperty("hdc")
  var hdc: String = "",
  @JsonProperty("order_status")
  var orderStatus: String? = "",
  @JsonProperty("pilot")
  var pilot: String? = "",
  @JsonProperty("subcategory")
  var subcategory: String? = "",
) {

  companion object {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val londonTimeZone = ZoneId.of("Europe/London")

    private fun getBritishDateAndTime(dateTime: ZonedDateTime?): String? =
      dateTime?.toInstant()?.atZone(londonTimeZone)?.format(dateTimeFormatter)

    private fun getBritishDate(dateTime: ZonedDateTime?): String? =
      dateTime?.toInstant()?.atZone(londonTimeZone)?.format(dateFormatter)

    fun fromOrder(order: Order, caseId: String?): MonitoringOrder {
      val conditions = order.monitoringConditions!!
      var monitoringStartDate = order.monitoringConditions!!.startDate
      var monitoringEndDate = order.monitoringConditions!!.endDate

      val monitoringConditions = sequence {
        yieldAll(order.enforcementZoneConditions)
        yieldAll(order.mandatoryAttendanceConditions)
        listOfNotNull(
          order.curfewConditions,
          order.monitoringConditionsTrail,
          order.monitoringConditionsAlcohol,
        ).forEach {
          yield(it)
        }
      }
      if (monitoringStartDate == null) {
        monitoringStartDate = monitoringConditions.mapNotNull { it.startDate }.minOrNull()
      }
      if (monitoringEndDate == null) {
        monitoringEndDate = monitoringConditions.mapNotNull { it.endDate }.maxOrNull()
      }

      val monitoringOrder = MonitoringOrder(
        deviceWearer = "${order.deviceWearer!!.firstName} ${order.deviceWearer!!.lastName}",
        orderType = conditions.orderType!!.value,
        orderRequestType = order.type.value,
        orderTypeDescription = conditions.orderTypeDescription?.value,
        orderStart = getBritishDateAndTime(monitoringStartDate),
        orderEnd = getBritishDateAndTime(monitoringEndDate) ?: "",
        serviceEndDate = getBritishDate(monitoringEndDate) ?: "",
        caseId = caseId,
        conditionType = conditions.conditionType!!.value,
        orderId = order.id.toString(),
        orderStatus = "Not Started",
        offence = getOffence(order),
        offenceAdditionalDetails = getOffenceAdditionalDetails(order),
        pilot = conditions.pilot?.value ?: "",

      )
      if (DataDictionaryVersion.isVersionSameOrAbove(order.dataDictionaryVersion, DataDictionaryVersion.DDV6)) {
        val isBail = conditions.orderType === OrderType.BAIL || conditions.orderType === OrderType.IMMIGRATION
        monitoringOrder.subcategory = RequestType.getSubCategory(order.type, isBail)
      }

      monitoringOrder.sentenceType = conditions.sentenceType?.value ?: ""
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

      if (order.interestedParties != null) {
        val interestedParties = order.interestedParties!!
        monitoringOrder.responsibleOfficerName = interestedParties.responsibleOfficerName
        monitoringOrder.responsibleOfficerPhone = getResponsibleOfficerPhoneNumber(order)
        monitoringOrder.responsibleOrganization = getResponsibleOrganisation(order)
        monitoringOrder.roRegion = getResponsibleOrganisationRegion(order)
        if (monitoringOrder.responsibleOrganization == ResponsibleOrganisation.PROBATION.value
        ) {
          monitoringOrder.pduResponsible = getProbationDeliveryUnit(order)
        }
        monitoringOrder.roEmail = interestedParties.responsibleOrganisationEmail
        monitoringOrder.notifyingOrganization = getNotifyingOrganisation(order)
        monitoringOrder.noName = getNotifyingOrganisationName(order)
        monitoringOrder.noEmail = interestedParties.notifyingOrganisationEmail
      }

      if (order.curfewConditions?.startDate != null) {
        val curfew = order.curfewConditions!!
        monitoringOrder.enforceableCondition!!.add(
          EnforceableCondition(
            "Curfew with EM",
            startDate = getBritishDateAndTime(curfew.startDate),
            endDate = getBritishDateAndTime(curfew.endDate) ?: "",
          ),
        )
        monitoringOrder.curfewDescription = curfew.curfewAdditionalDetails ?: ""
        monitoringOrder.conditionalReleaseDate = getBritishDate(order.curfewReleaseDateConditions?.releaseDate)
        monitoringOrder.conditionalReleaseStartTime = order.curfewReleaseDateConditions?.startTime ?: ""
        monitoringOrder.conditionalReleaseEndTime = order.curfewReleaseDateConditions?.endTime ?: ""
        monitoringOrder.curfewStart = getBritishDateAndTime(curfew.startDate)
        monitoringOrder.curfewEnd = getBritishDateAndTime(curfew.endDate)
        monitoringOrder.curfewDuration = getCurfewSchedules(order)
      }

      if (order.monitoringConditionsTrail?.startDate != null) {
        monitoringOrder.enforceableCondition!!.add(
          EnforceableCondition(
            "Location Monitoring (Fitted Device)",
            startDate = getBritishDateAndTime(order.monitoringConditionsTrail!!.startDate),
            endDate = getBritishDateAndTime(order.monitoringConditionsTrail!!.endDate),
          ),

        )
        monitoringOrder.trailMonitoring = "Yes"
      }

      if (order.enforcementZoneConditions.count() > 0) {
        monitoringOrder.enforceableCondition!!.add(
          EnforceableCondition(
            "EM Exclusion / Inclusion Zone",
            startDate = getBritishDateAndTime(monitoringStartDate),
            endDate = getBritishDateAndTime(monitoringEndDate) ?: "",
          ),
        )
        order.enforcementZoneConditions.forEach {
          if (it.zoneType == EnforcementZoneType.EXCLUSION) {
            monitoringOrder.exclusionZones.add(
              Zone(
                description = it.description,
                duration = it.duration,
                start = getBritishDate(it.startDate),
                end = getBritishDate(it.endDate) ?: "",
              ),
            )
          } else if (it.zoneType == EnforcementZoneType.INCLUSION) {
            monitoringOrder.inclusionZones.add(
              Zone(
                description = it.description,
                duration = it.duration,
                start = getBritishDate(it.startDate),
                end = getBritishDate(it.endDate) ?: "",
              ),
            )
          }
        }

        monitoringOrder.trailMonitoring = "No"
      }

      if (order.mandatoryAttendanceConditions.count() > 0) {
        monitoringOrder.enforceableCondition!!.add(
          EnforceableCondition(
            "Attendance Requirement",
            startDate = getBritishDateAndTime(monitoringStartDate) ?: "",
            endDate = getBritishDateAndTime(monitoringEndDate) ?: "",
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
          order.installationLocation?.location == InstallationLocationType.PRISON
        ) {
          monitoringOrder.tagAtSource = "true"
          monitoringOrder.tagAtSourceDetails = order.installationAppointment?.placeName ?: ""
          monitoringOrder.dateAndTimeInstallationWillTakePlace =
            getBritishDateAndTime(order.installationAppointment?.appointmentDate) ?: ""
        } else {
          monitoringOrder.tagAtSource = "false"
        }
      }

      getInstallationAddress(order)?.let {
        monitoringOrder.installationAddress1 = it.addressLine1
        monitoringOrder.installationAddress2 = it.addressLine2
        monitoringOrder.installationAddress3 = it.addressLine3
        monitoringOrder.installationAddress4 = it.addressLine4
        monitoringOrder.installationAddressPostcode = it.postcode
      }

      if (RequestType.VARIATION_TYPES.contains(order.type)) {
        if (order.type === RequestType.VARIATION) {
          monitoringOrder.orderVariationType = order.variationDetails!!.variationType?.value
        } else {
          monitoringOrder.orderVariationType = "OTHER"
        }
        monitoringOrder.orderVariationDate = order.variationDetails!!.variationDate.format(dateTimeFormatter)
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

    private fun getNotifyingOrganisation(order: Order): String {
      val notifyingOrganisation = order.interestedParties?.notifyingOrganisation
      val resolvedNotifyingOrganisation =
        when (order.dataDictionaryVersion) {
          DataDictionaryVersion.DDV4 -> NotifyingOrganisation.from(notifyingOrganisation)?.value
          else -> NotifyingOrganisationDDv5.from(notifyingOrganisation)?.value
        }

      return resolvedNotifyingOrganisation ?: notifyingOrganisation ?: "N/A"
    }

    private fun getNotifyingOrganisationName(order: Order): String {
      if (order.dataDictionaryVersion === DataDictionaryVersion.DDV4) {
        return Prison.from(order.interestedParties?.notifyingOrganisationName)?.value
          ?: CrownCourt.from(order.interestedParties?.notifyingOrganisationName)?.value
          ?: MagistrateCourt.from(order.interestedParties?.notifyingOrganisationName)?.value
          ?: order.interestedParties?.notifyingOrganisationName
          ?: ""
      }

      return CivilCountyCourtDDv5.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: CrownCourtDDv5.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: FamilyCourtDDv5.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: MagistrateCourtDDv5.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: MilitaryCourtDDv5.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: PrisonDDv5.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: ProbationServiceRegion.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: YouthCourtDDv5.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: YouthCustodyServiceRegionDDv5.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: order.interestedParties?.notifyingOrganisationName
        ?: ""
    }

    private fun getResponsibleOrganisation(order: Order): String =
      ResponsibleOrganisation.from(order.interestedParties?.responsibleOrganisation)?.value
        ?: order.interestedParties?.responsibleOrganisation
        ?: "N/A"

    private fun getResponsibleOrganisationRegion(order: Order): String =
      ProbationServiceRegion.from(order.interestedParties?.responsibleOrganisationRegion)?.value
        ?: YouthJusticeServiceRegions.from(order.interestedParties?.responsibleOrganisationRegion)?.value
        ?: order.interestedParties?.responsibleOrganisationRegion
        ?: ""

    private fun getProbationDeliveryUnit(order: Order): String {
      if (order.dataDictionaryVersion == DataDictionaryVersion.DDV6) {
        return ProbationDeliveryUnitsDDv6.from(order.probationDeliveryUnit?.unit)?.value ?: ""
      }
      return ProbationDeliveryUnits.from(order.probationDeliveryUnit?.unit)?.value ?: ""
    }

    private fun getOffence(order: Order): String? = Offence.from(order.installationAndRisk?.offence)?.value
      ?: order.installationAndRisk?.offence

    private fun getResponsibleOfficerPhoneNumber(order: Order): String? {
      if (order.interestedParties?.responsibleOfficerPhoneNumber == null) {
        return null
      }
      return PhoneNumberFormatter.formatAsInternationalDirectDialingNumber(
        order.interestedParties!!.responsibleOfficerPhoneNumber!!,
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

    private fun getOffenceAdditionalDetails(order: Order): String {
      val riskOffenceDetails = order.installationAndRisk?.offenceAdditionalDetails ?: ""
      val monitoringOffenceType = order.monitoringConditions?.offenceType ?: ""
      var monitoringPoliceArea = PoliceAreas.from(order.monitoringConditions?.policeArea)?.value

      if (monitoringPoliceArea == null) {
        monitoringPoliceArea = order.monitoringConditions?.policeArea ?: ""
      }

      val parts = listOfNotNull(
        riskOffenceDetails.takeIf { it.isNotBlank() },
        monitoringOffenceType.takeIf { it.isNotBlank() }?.let { "AC Offence: $it" },
        monitoringPoliceArea.takeIf { it.isNotBlank() }
          ?.let { "PFA: $it" },
      )

      return parts.joinToString(". ")
    }
  }
}

data class EnforceableCondition(
  val condition: String? = "",
  @JsonProperty("start_date")
  val startDate: String? = "",
  @JsonProperty("end_date")
  val endDate: String? = null,
)

data class CurfewSchedule(
  val location: String? = "",
  val allday: String? = "",
  val schedule: MutableList<Schedule>? = mutableListOf(),
)

data class Zone(
  val description: String? = "",
  val duration: String? = "",
  val start: String? = "",
  val end: String? = "",
)

data class Schedule(val day: String? = "", val start: String? = "", val end: String? = "") {
  companion object {
    private fun getShortDayString(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
      DayOfWeek.MONDAY -> "Mo"
      DayOfWeek.TUESDAY -> "Tu"
      DayOfWeek.WEDNESDAY -> "Wed"
      DayOfWeek.THURSDAY -> "Th"
      DayOfWeek.FRIDAY -> "Fr"
      DayOfWeek.SATURDAY -> "Sa"
      DayOfWeek.SUNDAY -> "Su"
    }

    fun fromCurfewTimeTable(curfewTimeTable: CurfewTimeTable): Schedule =
      Schedule(getShortDayString(curfewTimeTable.dayOfWeek), curfewTimeTable.startTime, curfewTimeTable.endTime)
  }
}
