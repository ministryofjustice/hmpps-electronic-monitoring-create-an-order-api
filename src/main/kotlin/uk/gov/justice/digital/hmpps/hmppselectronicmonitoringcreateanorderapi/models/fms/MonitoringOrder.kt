package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

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
  var offenceAdditionalDetails: String? = "",
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
  @JsonProperty("restriction_zones")
  var restrictionZones: MutableList<Zone> = mutableListOf(),
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
  @JsonProperty("dapol_missed_in_error")
  var dapolMissedInError: String? = "",
  @JsonProperty("ac_eligible_offences")
  var acEligibleOffences: MutableList<OffenceData>? = mutableListOf(),
  @JsonProperty("install_at_source_pilot")
  var installAtSourcePilot: String? = "",
  @JsonProperty("dapo_order_clause_numbers")
  var dapoOrderClauseNumbers: MutableList<DapoClause>? = mutableListOf(),
  @JsonProperty("offences")
  var offences: MutableList<OffenceData>? = mutableListOf(),

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("additional_information")
  var additionalInformation: String? = "",
) {
  companion object
}
