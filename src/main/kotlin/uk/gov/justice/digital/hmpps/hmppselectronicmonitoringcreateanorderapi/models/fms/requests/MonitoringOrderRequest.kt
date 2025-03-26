package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.CurfewSchedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.Schedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.Zone

open class MonitoringOrderRequest {

  open val abstinence: String = ""

  @JsonProperty("allday_lockdown")
  open val alldayLockdown: String = ""

  @JsonProperty("atv_allowance")
  open val atvAllowance: String = ""

  @JsonProperty("business_unit")
  open val businessUnit: String = ""

  @JsonProperty("case_id")
  open val caseId: String = ""

  @JsonProperty("checkin_schedule")
  open val checkinSchedule: MutableList<Schedule>? = mutableListOf()

  @JsonProperty("conditional_release_date")
  open val conditionalReleaseDate: String? = ""

  @JsonProperty("condition_type")
  open val conditionType: String = ""

  open val court: String = ""

  @JsonProperty("court_order_email")
  open val courtOrderEmail: String = ""

  @JsonProperty("crown_court_case_reference_number")
  open val crownCourtCaseReferenceNumber: String = ""

  @JsonProperty("curfew_description")
  open val curfewDescription: String = ""

  @JsonProperty("curfew_duration")
  open val curfewDuration: MutableList<CurfewSchedule> = mutableListOf()

  @JsonProperty("curfew_end")
  open val curfewEnd: String = ""

  @JsonProperty("curfew_start")
  open val curfewStart: String = ""

  @JsonProperty("device_type")
  open val deviceType: String = ""

  @JsonProperty("device_wearer")
  open val deviceWearer: String = ""

  @JsonProperty("enforceable_condition")
  open val enforceableCondition: MutableList<EnforceableCondition> = mutableListOf()

  @JsonProperty("exclusion_allday")
  open val exclusionAllday: String = ""

  @JsonProperty("exclusion_zones")
  open val exclusionZones: List<Zone> = mutableListOf()

  open val hdc: String = ""

  @JsonProperty("inclusion_zones")
  open val inclusionZones: List<Zone> = mutableListOf()

  @JsonProperty("installation_address_1")
  open val installationAddress1: String = ""

  @JsonProperty("installation_address_2")
  open val installationAddress2: String = ""

  @JsonProperty("installation_address_3")
  open val installationAddress3: String = ""

  @JsonProperty("installation_address_4")
  open val installationAddress4: String = ""

  @JsonProperty("installation_address_post_code")
  open val installationAddressPostcode: String = ""

  @JsonProperty("interim_court_date")
  open val interimCourtDate: String = ""

  open val issp: String = ""

  @JsonProperty("issuing_organisation")
  open val issuingOrganisation: String = ""

  @JsonProperty("magistrate_court_case_reference_number")
  open val magistrateCourtCaseReferenceNumber: String = ""

  @JsonProperty("media_interest")
  open val mediaInterest: String = ""

  @JsonProperty("new_order_received")
  open val newOrderReceived: String = ""

  @JsonProperty("no_address_1")
  open val noAddress1: String = ""

  @JsonProperty("no_address_2")
  open val noAddress2: String = ""

  @JsonProperty("no_address_3")
  open val noAddress3: String = ""

  @JsonProperty("no_address_4")
  open val noAddress4: String = ""

  @JsonProperty("no_email")
  open val noEmail: String = ""

  @JsonProperty("no_name")
  open val noName: String = ""

  @JsonProperty("no_phone_number")
  open val noPhoneNumber: String = ""

  @JsonProperty("no_post_code")
  open val noPostCode: String = ""

  @JsonProperty("notifying_officer_email")
  open val notifyingOfficerEmail: String = ""

  @JsonProperty("notifying_officer_name")
  open val notifyingOfficerName: String = ""

  @JsonProperty("notifying_organization")
  open val notifyingOrganization: String = ""

  open val offence: String? = null

  @JsonProperty("offence_date")
  open val offenceDate: String = ""

  @JsonProperty("order_end")
  open val orderEnd: String = ""

  @JsonProperty("order_id")
  open val orderId: String = ""

  @JsonProperty("order_request_type")
  open val orderRequestType: String = ""

  @JsonProperty("order_start")
  open val orderStart: String = ""

  @JsonProperty("order_status")
  open val orderStatus: String = "Not Started"

  @JsonProperty("order_type")
  open val orderType: String = ""

  @JsonProperty("order_type_description")
  open val orderTypeDescription: String? = null

  @JsonProperty("order_type_detail")
  open val orderTypeDetail: String = ""

  @JsonProperty("order_variation_date")
  open val orderVariationDate: String = ""

  @JsonProperty("order_variation_details")
  open val orderVariationDetails: String = ""

  @JsonProperty("order_variation_req_received_date")
  open val orderVariationReqReceivedDate: String = ""

  @JsonProperty("order_variation_type")
  open val orderVariationType: String = ""

  @JsonProperty("pdu_responsible")
  open val pduResponsible: String = ""

  @JsonProperty("pdu_responsible_email")
  open val pduResponsibleEmail: String = ""

  @JsonProperty("planned_order_end_date")
  open val plannedOrderEndDate: String = ""

  @JsonProperty("reason_for_order_ending_early")
  open val reasonForOrderEndingEarly: String = ""

  @JsonProperty("responsible_officer_details_received")
  open val responsibleOfficerDetailsReceived: String = ""

  @JsonProperty("responsible_officer_email")
  open val responsibleOfficerEmail: String = ""

  @JsonProperty("responsible_officer_name")
  open val responsibleOfficerName: String = ""

  @JsonProperty("responsible_officer_phone")
  open val responsibleOfficerPhone: String? = null

  @JsonProperty("responsible_organization")
  open val responsibleOrganization: String = ""

  @JsonProperty("revocation_date")
  open val revocationDate: String = ""

  @JsonProperty("revocation_type")
  open val revocationType: String = ""

  @JsonProperty("ro_address_1")
  open val roAddress1: String = ""

  @JsonProperty("ro_address_2")
  open val roAddress2: String = ""

  @JsonProperty("ro_address_3")
  open val roAddress3: String = ""

  @JsonProperty("ro_address_4")
  open val roAddress4: String = ""

  @JsonProperty("ro_email")
  open val roEmail: String = ""

  @JsonProperty("ro_phone")
  open val roPhone: String? = null

  @JsonProperty("ro_post_code")
  open val roPostCode: String = ""

  @JsonProperty("ro_region")
  open val roRegion: String = ""

  open val schedule: String = ""

  @JsonProperty("sentence_date")
  open val sentenceDate: String = ""

  @JsonProperty("service_end_date")
  open val serviceEndDate: String = ""

  @JsonProperty("sentence_expiry")
  open val sentenceExpiry: String = ""

  @JsonProperty("sentence_type")
  open val sentenceType: String = ""

  @JsonProperty("tag_at_source")
  open val tagAtSource: String = ""

  @JsonProperty("tag_at_source_details")
  open val tagAtSourceDetails: String = ""

  @JsonProperty("technical_bail")
  open val technicalBail: String = ""

  @JsonProperty("trial_date")
  open val trialDate: String = ""

  @JsonProperty("trial_outcome")
  open val trialOutcome: String = ""

  @JsonProperty("trail_monitoring")
  open val trailMonitoring: String = ""
}
