package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
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
  @JsonProperty("order_status")
  var orderStatus: String? = "",
) {

  companion object {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    fun fromOrder(order: Order, caseId: String?): MonitoringOrder {
      val conditions = order.monitoringConditions!!
      val monitoringOrder = MonitoringOrder(
        deviceWearer = "${order.deviceWearer!!.firstName} ${order.deviceWearer!!.lastName}",
        orderType = conditions.orderType!!.value,
        orderRequestType = order.type.value,
        orderTypeDescription = conditions.orderTypeDescription?.value,
        orderStart = conditions.startDate?.format(dateTimeFormatter),
        orderEnd = conditions.endDate?.format(dateTimeFormatter) ?: "",
        serviceEndDate = conditions.endDate?.format(dateFormatter) ?: "",
        caseId = caseId,
        conditionType = conditions.conditionType!!.value,
        orderId = order.id.toString(),
        orderStatus = "Not Started",
        offence = order.installationAndRisk?.offence,
      )

      if (conditions.curfew != null && conditions.curfew!!) {
        val curfew = order.curfewConditions!!
        monitoringOrder.enforceableCondition!!.add(
          EnforceableCondition(
            "Curfew with EM",
            startDate = curfew.startDate?.format(dateTimeFormatter),
            endDate = curfew.endDate?.format(dateTimeFormatter) ?: "",
          ),
        )
        monitoringOrder.curfewDescription = curfew.curfewDescription
        monitoringOrder.conditionalReleaseDate = order.curfewReleaseDateConditions?.releaseDate?.format(dateFormatter)
        monitoringOrder.curfewStart = curfew.startDate!!.format(dateTimeFormatter)
        monitoringOrder.curfewEnd = curfew.endDate?.format(dateTimeFormatter)
        monitoringOrder.curfewDuration = getCurfewSchedules(order, curfew)
      }

      if (conditions.trail != null && conditions.trail!!) {
        monitoringOrder.enforceableCondition!!.add(
          EnforceableCondition(
            "Location Monitoring (Fitted Device)",
            startDate = order.monitoringConditionsTrail!!.startDate?.format(dateTimeFormatter),
            endDate = order.monitoringConditionsTrail!!.endDate?.format(dateTimeFormatter),
          ),

        )
        monitoringOrder.trailMonitoring = "Yes"
      }

      if (conditions.exclusionZone != null && conditions.exclusionZone!!) {
        monitoringOrder.enforceableCondition!!.add(
          EnforceableCondition(
            "EM Exclusion / Inclusion Zone",
            startDate = conditions.startDate?.format(dateTimeFormatter),
            endDate = conditions.endDate?.format(dateTimeFormatter) ?: "",
          ),
        )
        order.enforcementZoneConditions.forEach {
          if (it.zoneType == EnforcementZoneType.EXCLUSION) {
            monitoringOrder.exclusionZones.add(
              Zone(
                description = it.description,
                duration = it.duration,
                start = it.startDate?.format(dateFormatter),
                end = it.endDate?.format(dateFormatter) ?: "",
              ),
            )
          } else if (it.zoneType == EnforcementZoneType.INCLUSION) {
            monitoringOrder.inclusionZones.add(
              Zone(
                description = it.description,
                duration = it.duration,
                start = it.startDate?.format(dateFormatter),
                end = it.endDate?.format(dateFormatter) ?: "",
              ),
            )
          }
        }

        monitoringOrder.trailMonitoring = "No"
      }

      // TODO: wait for confirmation if mandatory attendance is required
//      if(conditions.mandatoryAttendance!=null && conditions.mandatoryAttendance!!){
//        mo.enforceableCondition!!.add(EnforceableCondition("Attendance requirement"))
//      }

      if (conditions.alcohol != null && conditions.alcohol!!) {
        val condition = order.monitoringConditionsAlcohol!!
        if (condition.monitoringType == AlcoholMonitoringType.ALCOHOL_ABSTINENCE) {
          monitoringOrder.enforceableCondition!!.add(
            EnforceableCondition(
              "AAMR",
              startDate = condition.startDate?.format(dateTimeFormatter),
              endDate = condition.endDate?.format(dateTimeFormatter) ?: "",
            ),
          )
          monitoringOrder.abstinence = "Yes"
        } else {
          monitoringOrder.enforceableCondition!!.add(
            EnforceableCondition(
              "AML",
              startDate = condition.startDate?.format(dateTimeFormatter),
              endDate = condition.endDate?.format(dateTimeFormatter) ?: "",
            ),
          )
          monitoringOrder.abstinence = "No"
        }

        if (!condition.prisonName.isNullOrBlank()) {
          monitoringOrder.tagAtSourceDetails = condition.prisonName
        } else if (!condition.probationOfficeName.isNullOrBlank()) {
          monitoringOrder.tagAtSourceDetails = condition.probationOfficeName
        }
      }

      if (order.interestedParties != null) {
        val interestedParties = order.interestedParties!!
        monitoringOrder.responsibleOfficerName = interestedParties.responsibleOfficerName
        monitoringOrder.responsibleOfficerPhone = interestedParties.responsibleOfficerPhoneNumber
        monitoringOrder.responsibleOrganization = if (interestedParties.responsibleOrganisation == "") {
          "N/A"
        } else {
          interestedParties.responsibleOrganisation
        }
        monitoringOrder.roRegion = interestedParties.responsibleOrganisationRegion
        monitoringOrder.roPhone = interestedParties.responsibleOrganisationPhoneNumber
        monitoringOrder.roEmail = interestedParties.responsibleOrganisationEmail
        monitoringOrder.notifyingOrganization = if (interestedParties.notifyingOrganisation == "") {
          "N/A"
        } else {
          interestedParties.notifyingOrganisation
        }
        val address = order.addresses.firstOrNull { it.addressType == AddressType.RESPONSIBLE_ORGANISATION }
        if (address != null) {
          monitoringOrder.roAddress1 = address.addressLine1
          monitoringOrder.roAddress2 = address.addressLine2
          monitoringOrder.roAddress3 = address.addressLine3
          monitoringOrder.roAddress4 = address.addressLine4
          monitoringOrder.roPostCode = address.postcode
        }
        monitoringOrder.noName = interestedParties.notifyingOrganisationName
        monitoringOrder.noEmail = interestedParties.notifyingOrganisationEmail
      }

      order.addresses.firstOrNull { it.addressType == AddressType.INSTALLATION }?.let {
        monitoringOrder.installationAddress1 = it.addressLine1
        monitoringOrder.installationAddress2 = it.addressLine2
        monitoringOrder.installationAddress3 = it.addressLine3
        monitoringOrder.installationAddress4 = it.addressLine4
        monitoringOrder.installationAddressPostcode = it.postcode
      }

      if (order.type === RequestType.VARIATION) {
        monitoringOrder.orderVariationType = order.variationDetails!!.variationType.value
        monitoringOrder.orderVariationDate = order.variationDetails!!.variationDate.format(dateTimeFormatter)
      }

      return monitoringOrder
    }

    private fun getCurfewSchedules(order: Order, curfew: CurfewConditions): MutableList<CurfewSchedule> {
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
      return schedules
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
