package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import java.time.DayOfWeek
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
  @JsonProperty("describe_exclusion")
  var describeExclusion: String? = "",
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
  @JsonProperty("tag_at_source")
  var tagAtSource: String? = "",
  @JsonProperty("tag_at_source_details")
  var tagAtSourceDetails: String? = "",
  @JsonProperty("technical_bail")
  var technicalBail: String? = "",
  @JsonProperty("trial_date")
  var trialDate: String? = "",
  @JsonProperty("trial_outcome")
  var trialOutcome: String? = "",
  @JsonProperty("conditional_release_date")
  var conditionalReleaseDate: String? = "",
  @JsonProperty("reason_for_order_ending_early")
  var reasonForOrderEndingEarly: String? = "",
  @JsonProperty("business_unit")
  var businessUnit: String? = "",
  @JsonProperty("service_end_date")
  var serviceEndDate: String? = "",
  @JsonProperty("curfew_start")
  var curfewStart: String? = "",
  @JsonProperty("curfew_end")
  var curfewEnd: String? = "",
  @JsonProperty("curfew_duration")
  var curfewDuration: MutableList<CurfewSchedule>? = mutableListOf(),
  @JsonProperty("trail_monitoring")
  var trailMonitoring: String? = "",
  @JsonProperty("exclusion_zones")
  var exclusionZones: String? = "",
  @JsonProperty("exclusion_zones_duration")
  var exclusionZonesDuration: String? = "",
  @JsonProperty("inclusion_zones")
  var inclusionZones: String? = "",
  @JsonProperty("inclusion_zones_duration")
  var inclusionZonesDuration: String? = "",
  @JsonProperty("abstinence")
  var abstinence: String? = "",
  @JsonProperty("schedule")
  var schedule: String? = "",
  @JsonProperty("checkin_schedule")
  var checkinSchedule: String? = "",
  @JsonProperty("revocation_date")
  var revocationDate: String? = "",
  @JsonProperty("revocation_type")
  var revocationType: String? = "",
  @JsonProperty("order_status")
  var orderStatus: String? = "",
) {

  companion object {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    fun fromOrder(order: Order): MonitoringOrder {
      val conditions = order.monitoringConditions!!
      val monitoringOrder = MonitoringOrder(
        deviceWearer = order.fmsDeviceWearerId,
        orderType = conditions.orderType,
        deviceType = conditions.devicesRequired?.joinToString { "," },
        orderStart = conditions.startDate?.format(formatter),
        orderEnd = conditions.endDate?.format(formatter),
        serviceEndDate = conditions.endDate?.format(formatter),
        caseId = conditions.caseId,
        conditionType = conditions.conditionType,
        orderId = order.id.toString(),
        orderStatus = "Not Started",
      )

      if (conditions.curfew != null && conditions.curfew!!) {
        val curfew = order.curfewConditions!!
        monitoringOrder.enforceableCondition!!.add(EnforceableCondition("Curfew with EM"))
        monitoringOrder.conditionalReleaseDate = order.curfewReleaseDateConditions!!.releaseDate!!.format(formatter)
        monitoringOrder.curfewStart = curfew.startDate!!.format(formatter)
        monitoringOrder.curfewEnd = curfew.endDate!!.format(formatter)
        monitoringOrder.curfewDuration = getCurfewSchedules(order, curfew)
      }

      if (conditions.trail != null && conditions.trail!!) {
        if (conditions.devicesRequired!!.contains("Location - fitted")) {
          monitoringOrder.enforceableCondition!!.add(EnforceableCondition("Location Monitoring (Fitted Device)"))
        } else {
          monitoringOrder.enforceableCondition!!.add(EnforceableCondition("Location Monitoring (using Non-Fitted Device)"))
        }
        monitoringOrder.trailMonitoring = "true"
      }

      if (conditions.exclusionZone != null && conditions.exclusionZone!!) {
        monitoringOrder.enforceableCondition!!.add(EnforceableCondition("EM Exclusion / Inclusion Zone"))
        val condition = order.enforcementZoneConditions.first()
        if (condition.zoneType == EnforcementZoneType.EXCLUSION) {
          monitoringOrder.exclusionZones = "true"
          monitoringOrder.describeExclusion = condition.description
        } else if (condition.zoneType == EnforcementZoneType.INCLUSION) {
          monitoringOrder.inclusionZones = "true"
        }
        monitoringOrder.inclusionZonesDuration = condition.duration
      }

      // TODO: wait for confirmation if mandatory attendance is required
//      if(conditions.mandatoryAttendance!=null && conditions.mandatoryAttendance!!){
//        mo.enforceableCondition!!.add(EnforceableCondition("Attendance requirement"))
//      }

      if (conditions.alcohol != null && conditions.alcohol!!) {
        val condition = order.alcoholMonitoringConditions!!
        if (condition.monitoringType == AlcoholMonitoringType.ALCOHOL_ABSTINENCE) {
          monitoringOrder.enforceableCondition!!.add(EnforceableCondition("AAMR"))
          monitoringOrder.abstinence = "true"
        } else {
          monitoringOrder.enforceableCondition!!.add(EnforceableCondition("AML"))
          monitoringOrder.abstinence = "false"
        }

        if (!condition.prisonName.isNullOrBlank()) {
          monitoringOrder.tagAtSourceDetails = condition.prisonName
        } else if (!condition.probationOfficeName.isNullOrBlank()) {
          monitoringOrder.tagAtSourceDetails = condition.probationOfficeName
        }
      }

      if (order.responsibleOfficer != null) {
        val responsibleOfficer = order.responsibleOfficer!!
        monitoringOrder.responsibleOfficerName = responsibleOfficer.name
        monitoringOrder.responsibleOfficerPhone = responsibleOfficer.phoneNumber
        monitoringOrder.responsibleOrganization = responsibleOfficer.organisation
        monitoringOrder.roRegion = responsibleOfficer.organisationRegion
        monitoringOrder.roPostCode = responsibleOfficer.organisationPostCode
        monitoringOrder.roPhone = responsibleOfficer.organisationPhoneNumber
        monitoringOrder.roEmail = responsibleOfficer.organisationEmail
        monitoringOrder.notifyingOrganization = responsibleOfficer.notifyingOrganisation
      }

      return monitoringOrder
    }

    private fun getCurfewSchedules(order: Order, curfew: CurfewConditions): MutableList<CurfewSchedule> {
      val schedules = mutableListOf<CurfewSchedule>()
      val primaryAddressTimeTable = order.curfewTimeTable.filter { it.curfewAddress!!.uppercase().contains("PRIMARY_ADDRESS") }
      schedules.add(CurfewSchedule(location = "primary", "", primaryAddressTimeTable.map { Schedule.fromCurfewTimeTable(it) }.toMutableList()))
      val secondaryAddress = order.addresses.firstOrNull { it.addressType === AddressType.SECONDARY }
      if (secondaryAddress != null) {
        val secondaryTimeTable = order.curfewTimeTable.filter { it.curfewAddress!!.uppercase().contains("SECONDARY_ADDRESS") }
        schedules.add(CurfewSchedule(location = "secondary", "", secondaryTimeTable.map { Schedule.fromCurfewTimeTable(it) }.toMutableList()))
      }
      return schedules
    }
  }
}

data class EnforceableCondition(
  val condition: String? = "",
)

data class CurfewSchedule(
  val location: String? = "",
  val allday: String? = "",
  val schedule: MutableList<Schedule>? = mutableListOf(),
)

data class Schedule(
  val day: String? = "",
  val start: String? = "",
  val end: String? = "",
) {
  companion object {
    private fun getShortDayString(dayOfWeek: DayOfWeek): String {
      return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Mo"
        DayOfWeek.TUESDAY -> "Tu"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Th"
        DayOfWeek.FRIDAY -> "Fr"
        DayOfWeek.SATURDAY -> "Sa"
        DayOfWeek.SUNDAY -> "Su"
      }
    }
    fun fromCurfewTimeTable(curfewTimeTable: CurfewTimeTable): Schedule {
      return Schedule(getShortDayString(curfewTimeTable.dayOfWeek), curfewTimeTable.startTime, curfewTimeTable.endTime)
    }
  }
}
