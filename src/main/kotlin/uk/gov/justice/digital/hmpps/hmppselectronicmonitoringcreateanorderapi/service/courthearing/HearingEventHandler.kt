package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Defendant
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Gender
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Hearing
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.HearingEvent
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.JudicialResults
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.JudicialResultsPrompt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.enums.BailOrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.enums.CommunityOrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.FmsService
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class HearingEventHandler(
  private val fmsService: FmsService,
  private val eventService: EventService,
) {
  private val commentPlatformUsername = "COMMENT_PLATFORM"
  private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  companion object {

    //region Common platform order types

    const val COMMUNITY_ORDER_ENGLAND_AND_WALES_UUID = "418b3aa7-65ab-4a4a-bab9-2f96b698118c"

    private const val COMMUNITY_ORDER_SCOTLAND_UUID = "ae617390-b41e-46ac-bd63-68a28512676a"

    const val BAIL_ADULT_REMITTAL_FOR_SENTENCE_ON_CONDITIONAL = "f917ba0c-1faf-4945-83a8-50be9049f9b4"

    const val BAIL_CROWN_COURT_SENTENCE_IN_CUSTODY_WITH_BAIL_DIRECTION = "35430208-3705-44ce-b5d5-153c0337f6ab"

    const val BAIL_REMAND_IN_CARE_OF_LOCAL_AUTHORITY = "f666fd58-36c5-493f-aa11-89714faee6e6"

    const val BAIL_REMANDED_IN_CUSTODY_WITH_BAIL_DIRECTION = "e26940b7-2534-42f2-9c44-c70072bf6ad2"

    const val BAIL_REMANDED_ON_CONDITIONAL_BAIL = "3a529001-2f43-45ba-a0a8-d3ced7e9e7ad"

    val BAIL_CONDITION_UUIDs = arrayOf(
      BAIL_ADULT_REMITTAL_FOR_SENTENCE_ON_CONDITIONAL,
      BAIL_CROWN_COURT_SENTENCE_IN_CUSTODY_WITH_BAIL_DIRECTION,
      BAIL_REMAND_IN_CARE_OF_LOCAL_AUTHORITY,
      BAIL_REMANDED_IN_CUSTODY_WITH_BAIL_DIRECTION,
      BAIL_REMANDED_ON_CONDITIONAL_BAIL,
    )

    // Suspended sentence order
    const val SSO_YOUNG_OFFENDER_INSTITUTION_DETENTION_UUID = "5679e5b7-0ca8-4d2a-ba80-7a50025fb589"

    const val SSO_IMPRISONMENT_UUID = "8b1cff00-a456-40da-9ce4-f11c20959084"

    // end region
    fun isEnglandAdnWalesEMRequest(offence: Offence): Boolean {
      return !offence.judicialResults.any {
          judicialResults ->
        judicialResults.judicialResultTypeId== COMMUNITY_ORDER_SCOTLAND_UUID
      } &&
        offence.judicialResults.any {
            judicialResults ->
          CommunityOrderType.from(judicialResults.judicialResultTypeId) != null ||
            (
              BAIL_CONDITION_UUIDs.contains(judicialResults.judicialResultTypeId) &&
                judicialResults.judicialResultPrompts.any {
                  BailOrderType.from(it.judicialResultPromptTypeId) != null
                }
              )
        }
    }
  }

  fun handleHearingEvent(event: HearingEvent): List<String> {
    val result = mutableListOf<String>()
    val orders = getOrdersFromHearing(event.hearing)
    val startTimeInMs = System.currentTimeMillis()
    val startDateTime = ZonedDateTime.now(ZoneId.of("GMT"))
    orders.forEach { order ->
      run {
        val submitResult = fmsService.submitOrder(order, FmsOrderSource.COMMON_PLATFORM)

        if (!submitResult.success) {
          val fullName = " ${order.deviceWearer!!.firstName} ${order.deviceWearer!!.lastName}"
          result.add("Error create order for $fullName, error: ${submitResult.error} ")
          eventService.recordEvent(
            "Common_Platform_Failed_Request",
            mapOf(
              "Error" to "${submitResult.error}",
              "Start Date And Time" to startDateTime.format(formatter),
            ),
            System.currentTimeMillis() - startTimeInMs,
          )
        } else {
          eventService.recordEvent(
            "Common_Platform_Success_Request",
            mapOf(
              "OrderType" to order.monitoringConditions!!.orderType!!,
              "Start Date And Time" to startDateTime.format(formatter),
            ),
            System.currentTimeMillis() - startTimeInMs,
          )
        }
      }
    }
    return result
  }

  fun getOrdersFromHearing(hearing: Hearing): List<Order> {
    // Get defendant that has taggable offences
    val defendantOffences = hearing.prosecutionCases
      .flatMap { it.defendants }
      .filter { defendant -> defendant.offences.any { isEnglandAdnWalesEMRequest(it) } }
      .groupBy { it }
      .mapValues { (_, defendants) ->
        defendants.flatMap { it.offences }.filter { isEnglandAdnWalesEMRequest(it) }.toMutableList()
      }.toMap()

    // map each defendant to Order
    return defendantOffences.map { (defendant, offences) ->
      getOrderForDefendant(hearing, defendant, offences)
    }
  }

  private fun getOrderForDefendant(hearing: Hearing, defendant: Defendant, offences: List<Offence>): Order {
    val judicialResults = offences.flatMap { it.judicialResults }.toList()

    val prompts = judicialResults.flatMap { it.judicialResultPrompts }.toList()
    val order = Order(username = commentPlatformUsername, status = OrderStatus.IN_PROGRESS, type = OrderType.REQUEST)

    val monitoringConditions = MonitoringConditions(orderId = order.id)
    val orderedDate = judicialResults.first().orderedDate
    monitoringConditions.startDate = ZonedDateTime.of(orderedDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))

    monitoringConditions.conditionType = getConditionType(judicialResults)
    monitoringConditions.orderType = getOrderType(judicialResults)

    if (monitoringConditions.conditionType == MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER) {
      loadCommunityOrderConditions(judicialResults, order, monitoringConditions, prompts, hearing)
    } else if (monitoringConditions.conditionType == MonitoringConditionType.BAIL_ORDER) {
      loadBailOrderConditions(judicialResults, order, monitoringConditions, prompts, hearing)
    }

    order.monitoringConditions = monitoringConditions

    val person = defendant.personDefendant?.personDetails
    val deviceWearer = DeviceWearer(orderId = order.id)

    deviceWearer.dateOfBirth = ZonedDateTime.of(person?.dateOfBirth, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
    deviceWearer.firstName = person?.firstName
    deviceWearer.lastName = person?.lastName
    deviceWearer.sex = getSex(person?.gender)
    deviceWearer.adultAtTimeOfInstallation = !defendant.isYouth

    val address = person?.address
    if (address != null) {
      deviceWearer.noFixedAbode = false
      order.addresses.add(
        Address(
          orderId = order.id,
          addressType = AddressType.PRIMARY,
          addressLine1 = address.address1,
          addressLine2 = address.address2 ?: "N/A",
          addressLine3 = address.address3 ?: "N/A",
          addressLine4 = address.address4 ?: "N/A",
          postcode = address.postcode ?: "N/A",
        ),
      )
    }
    order.deviceWearer = deviceWearer

    val contact = person?.contact
    if (contact != null) {
      val contactDetails =
        ContactDetails(orderId = order.id, contactNumber = contact.mobile ?: contact.home ?: contact.work ?: "")
      order.contactDetails = contactDetails
    }

    return order
  }

  private fun loadCommunityOrderConditions(
    judicialResults: List<JudicialResults>,
    order: Order,
    monitoringConditions: MonitoringConditions,
    prompts: List<JudicialResultsPrompt>,
    hearing: Hearing,
  ) {
    if (judicialResults.any { it.judicialResultTypeId == CommunityOrderType.ALCOHOL_ABSTAIN_MONITORING.uuid }) {
      monitoringConditions.alcohol = true
      val alcoholConditions = AlcoholMonitoringConditions(
        orderId = order.id,
        monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
        startDate = monitoringConditions.startDate,
        endDate = getPromptValue(prompts, "Until")?.let {
          val localDate = LocalDate.parse(it, formatter)
          ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
        },
      )

      monitoringConditions.endDate = alcoholConditions.endDate
      order.monitoringConditionsAlcohol = alcoholConditions
    }

    val exclusionZoneJudicialResult = judicialResults.firstOrNull {
      it.judicialResultTypeId == CommunityOrderType.EXCLUSION_ZONE.uuid
    }
    if (exclusionZoneJudicialResult != null) {
      monitoringConditions.exclusionZone = true
      val zone =
        getCommunityOrderEnforcementZone(order.id, exclusionZoneJudicialResult, prompts, EnforcementZoneType.EXCLUSION)
      monitoringConditions.startDate = zone.startDate
      monitoringConditions.endDate = zone.endDate
      order.enforcementZoneConditions.add(zone)
    }

    val inclusionZoneJudicialResults = judicialResults.firstOrNull {
      it.judicialResultTypeId == CommunityOrderType.INCLUSION_ZONE.uuid
    }
    if (inclusionZoneJudicialResults != null) {
      monitoringConditions.exclusionZone = true
      val zone =
        getCommunityOrderEnforcementZone(order.id, inclusionZoneJudicialResults, prompts, EnforcementZoneType.INCLUSION)
      monitoringConditions.startDate = zone.startDate
      monitoringConditions.endDate = zone.endDate
      order.enforcementZoneConditions.add(zone)
    }

    if (judicialResults.any { it.judicialResultTypeId == CommunityOrderType.TRAIL_MONITORING.uuid }) {
      monitoringConditions.trail = true
      val trailCondition = TrailMonitoringConditions(orderId = order.id)
      val startTime = getPromptValue(prompts, "Start time of tagging") ?: "00:00"

      val startDate = getPromptValue(
        prompts,
        "The defendant's whereabouts are to be electronically monitored. Start date",
      )?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.parse(startTime), ZoneId.of("GMT"))
      }

      trailCondition.startDate = startDate
      monitoringConditions.startDate = startDate
      val endTime = getPromptValue(prompts, "End time of tagging") ?: "00:00"
      val endDate =
        getPromptValue(prompts, "End date of tagging")?.let {
          val localDate = LocalDate.parse(it, formatter)
          ZonedDateTime.of(localDate, LocalTime.parse(endTime), ZoneId.of("GMT"))
        }
      trailCondition.endDate = endDate
      monitoringConditions.endDate = endDate
      order.monitoringConditionsTrail = trailCondition
    }

    if (judicialResults.any { it.judicialResultTypeId == CommunityOrderType.COMMUNITY_ORDER_CURFEW.uuid }) {
      monitoringConditions.curfew = true
      val condition = CurfewConditions(orderId = order.id)
      val startTime = getPromptValue(prompts, "Start time of tagging") ?: "00:00"
      condition.startDate = getPromptValue(prompts, "Start date of tagging")?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.parse(startTime), ZoneId.of("GMT"))
      }
      val endTime = getPromptValue(prompts, "End time of tagging") ?: "00:00"
      condition.endDate = getPromptValue(prompts, "End date of tagging")?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.parse(endTime), ZoneId.of("GMT"))
      }
      order.curfewConditions = condition
    }

    //region InterestedParties
    val responsibleOrganisation = getResponsibleOrganisation(
      getPromptValue(
        prompts,
        "Responsible officer",
      ),
    )
    val responsibleOrganisationRegion = getPromptValue(
      prompts,
      "Probation team to be notified organisation name",
    ) ?: ""
    val responsibleOrganisationEmail = getPromptValue(
      prompts,
      "Probation team to be notified email address 1",
    ) ?: ""

    order.interestedParties = buildInterestedPartiesFromHearing(
      order.id,
      responsibleOrganisation,
      responsibleOrganisationRegion,
      responsibleOrganisationEmail,
      hearing.courtCentre.name,
    )
    //endregion
  }

  private fun loadBailOrderConditions(
    judicialResults: List<JudicialResults>,
    order: Order,
    monitoringConditions: MonitoringConditions,
    prompts: List<JudicialResultsPrompt>,
    hearing: Hearing,
  ) {
    monitoringConditions.endDate = getNextCourtHearingDate(prompts)
    judicialResults.firstOrNull {
      it.judicialResultPrompts.any { prompts -> prompts.judicialResultPromptTypeId == BailOrderType.CURFEW.uuid }
    }?.let {
      monitoringConditions.curfew = true
      val condition = CurfewConditions(orderId = order.id)
      condition.startDate = ZonedDateTime.of(it.orderedDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
      condition.endDate = monitoringConditions.endDate
      order.curfewConditions = condition
    }

    judicialResults.firstOrNull {
      it.judicialResultPrompts.any { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.EXCLUSION_NOT_ENTER_A_PLACE.uuid
      }
    }?.let {
      val conditionPrompt = it.judicialResultPrompts.first { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.EXCLUSION_NOT_ENTER_A_PLACE.uuid
      }
      monitoringConditions.exclusionZone = true
      val condition =
        getBailOrderEnforcementZone(conditionPrompt, it.orderedDate, EnforcementZoneType.EXCLUSION, order.id)
      condition.endDate = monitoringConditions.endDate
      order.enforcementZoneConditions.add(condition)
    }

    judicialResults.firstOrNull {
      it.judicialResultPrompts.any { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.EXCLUSION_EXCEPT_COURT_OR_APPOINTMENT.uuid
      }
    }?.let {
      val conditionPrompt = it.judicialResultPrompts.first { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.EXCLUSION_EXCEPT_COURT_OR_APPOINTMENT.uuid
      }
      monitoringConditions.exclusionZone = true
      val condition =
        getBailOrderEnforcementZone(conditionPrompt, it.orderedDate, EnforcementZoneType.EXCLUSION, order.id)
      condition.endDate = monitoringConditions.endDate
      order.enforcementZoneConditions.add(condition)
    }

    judicialResults.firstOrNull {
      it.judicialResultPrompts.any { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.INCLUSION_SPECIFIED_RADIUS.uuid
      }
    }?.let {
      val conditionPrompt = it.judicialResultPrompts.first { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.INCLUSION_SPECIFIED_RADIUS.uuid
      }
      monitoringConditions.exclusionZone = true
      val condition =
        getBailOrderEnforcementZone(conditionPrompt, it.orderedDate, EnforcementZoneType.INCLUSION, order.id)
      condition.endDate = monitoringConditions.endDate
      order.enforcementZoneConditions.add(condition)
    }

    judicialResults.firstOrNull {
      it.judicialResultPrompts.any { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.INCLUSION_NOT_TO_LEAVE.uuid
      }
    }?.let {
      val conditionPrompt = it.judicialResultPrompts.first { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.INCLUSION_NOT_TO_LEAVE.uuid
      }
      monitoringConditions.exclusionZone = true
      val condition =
        getBailOrderEnforcementZone(conditionPrompt, it.orderedDate, EnforcementZoneType.INCLUSION, order.id)
      condition.endDate = monitoringConditions.endDate
      order.enforcementZoneConditions.add(condition)
    }

    //region InterestedParties

    var responsibleOrganisation = ""
    val responsibleOrganisationRegion = getPromptValue(
      prompts,
      "Probation / YOT to be notified organisation name",
    ) ?: ""

    if (responsibleOrganisationRegion != "") {
      responsibleOrganisation = "Probation"
    }

    val responsibleOrganisationEmail = getPromptValue(
      prompts,
      "Probation / YOT to be notified email address 1",
    ) ?: getPromptValue(
      prompts,
      "Prison email address 1",
    ) ?: ""

    order.interestedParties = buildInterestedPartiesFromHearing(
      order.id,
      responsibleOrganisation,
      responsibleOrganisationRegion,
      responsibleOrganisationEmail,
      hearing.courtCentre.name,
    )
    //endregion
  }

  private fun getNextCourtHearingDate(prompts: List<JudicialResultsPrompt>): ZonedDateTime {
    var nextHearingDetails: String = ""
    var nextHearingDateKey = ""
    if (prompts.any { it.label == "Next hearing in magistrates' court" }) {
      nextHearingDetails = getPromptValue(prompts, "Next hearing in magistrates' court") ?: ""
      nextHearingDateKey = "Date of hearing"
    } else if (prompts.any { it.label == "Next hearing in Crown Court" }) {
      nextHearingDetails = getPromptValue(prompts, "Next hearing in Crown Court") ?: ""
      nextHearingDateKey = "Fixed Date"
    }
    val nextHearingDetailsAsMap = nextHearingDetails.split("\n").associate {
      val parts = it.split(":", limit = 2)
      parts[0] to parts[1]
    }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val localDate = LocalDate.parse(nextHearingDetailsAsMap[nextHearingDateKey] ?: "", formatter)
    return ZonedDateTime.of(
      localDate,
      LocalTime.parse(nextHearingDetailsAsMap["Time of hearing"] ?: ""),
      ZoneId.of("GMT"),
    )
  }

  private fun getBailOrderEnforcementZone(
    conditionPrompt: JudicialResultsPrompt,
    startDate: LocalDate?,
    zoneType: EnforcementZoneType,
    orderId: UUID,
  ): EnforcementZoneConditions {
    val condition = EnforcementZoneConditions(orderId = orderId)
    condition.zoneType = zoneType

    condition.description = "${conditionPrompt.label} ${conditionPrompt.value}"
    condition.startDate = ZonedDateTime.of(startDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
    return condition
  }

  private fun getCommunityOrderEnforcementZone(
    orderId: UUID,
    judicialResult: JudicialResults,
    prompts: List<JudicialResultsPrompt>,
    zoneType: EnforcementZoneType,
  ): EnforcementZoneConditions {
    val zone = EnforcementZoneConditions(orderId = orderId)
    zone.zoneType = zoneType
    val startTime = getPromptValue(prompts, "Start time for tag") ?: "00:00"

    val startDate = getPromptValue(prompts, "Start date for tag")?.let {
      val localDate = LocalDate.parse(it, formatter)
      ZonedDateTime.of(localDate, LocalTime.parse(startTime), ZoneId.of("GMT"))
    }

    zone.startDate = startDate
    val endTime = getPromptValue(prompts, "End time for tag") ?: "00:00"
    val endDate =
      getPromptValue(prompts, "End date for tag")?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.parse(endTime), ZoneId.of("GMT"))
      }

    zone.endDate = endDate
    zone.duration = getPromptValue(prompts, "Exclusion and electronic monitoring period")
    zone.zoneLocation = getPromptValue(prompts, "Place / area")
    zone.description = judicialResult.resultText

    return zone
  }

  private fun getOrderType(results: List<JudicialResults>): String? {
    if (results.any {
        it.judicialResultTypeId == COMMUNITY_ORDER_ENGLAND_AND_WALES_UUID ||
          it.judicialResultTypeId == SSO_YOUNG_OFFENDER_INSTITUTION_DETENTION_UUID ||
          it.judicialResultTypeId == SSO_IMPRISONMENT_UUID
      }
    ) {
      return "Community"
    } else if (results.any {
        it.judicialResultTypeId == BAIL_ADULT_REMITTAL_FOR_SENTENCE_ON_CONDITIONAL ||
          it.judicialResultTypeId == BAIL_REMAND_IN_CARE_OF_LOCAL_AUTHORITY ||
          it.judicialResultTypeId == BAIL_REMANDED_ON_CONDITIONAL_BAIL
      }
    ) {
      return "Pre-Trial"
    } else if (results.any {
        it.judicialResultTypeId == BAIL_CROWN_COURT_SENTENCE_IN_CUSTODY_WITH_BAIL_DIRECTION ||
          it.judicialResultTypeId == BAIL_REMANDED_IN_CUSTODY_WITH_BAIL_DIRECTION
      }
    ) {
      return "Post Release"
    }
    return null
  }

  private fun getConditionType(results: List<JudicialResults>): MonitoringConditionType? {
    if (results.any {
        it.judicialResultTypeId == COMMUNITY_ORDER_ENGLAND_AND_WALES_UUID ||
          it.judicialResultTypeId == SSO_YOUNG_OFFENDER_INSTITUTION_DETENTION_UUID ||
          it.judicialResultTypeId == SSO_IMPRISONMENT_UUID
      }
    ) {
      return MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER
    } else if (results.any { BAIL_CONDITION_UUIDs.contains(it.judicialResultTypeId) }

    ) {
      return MonitoringConditionType.BAIL_ORDER
    }

    return null
  }

  private fun getSex(gender: Gender?): String {
    return when (gender) {
      Gender.MALE -> "male"
      Gender.FEMALE -> "female"
      Gender.NOT_KNOWN -> "unknown"
      Gender.NOT_SPECIFIED -> "prefer not to say"
      null -> ""
    }
  }

  private fun getResponsibleOrganisation(responsibleOfficer: String?): String {
    return when (responsibleOfficer) {
      "an officer of a provider of probation services" -> "Probation"
      "a probation officer" -> "Probation"
      "a member of the youth offending team" -> "YJB"
      "the electronic monitoring supervisor" -> "Field Monitoring Service"
      "the officer in charge of the Attendance Centre" -> "Policy/youth attendance"
      else -> ""
    }
  }

  private fun getPromptValue(prompts: List<JudicialResultsPrompt>, label: String): String? {
    return prompts.firstOrNull { it.label == label }?.value
  }

  private fun buildInterestedPartiesFromHearing(
    orderId: UUID,
    responsibleOfficer: String,
    responsibleOrganisationRegion: String,
    responsibleOrganisationId: String,
    notifyingOrganisation: String,
  ): InterestedParties {
    return InterestedParties(
      orderId = orderId,
      responsibleOrganisation = responsibleOfficer,
      responsibleOrganisationRegion = responsibleOrganisationRegion,
      responsibleOrganisationEmail = responsibleOrganisationId,
      notifyingOrganisation = notifyingOrganisation,
      responsibleOrganisationAddress =
      Address(
        orderId = orderId,
        addressType = AddressType.RESPONSIBLE_ORGANISATION,
        addressLine1 = "",
        addressLine2 = "",
        postcode = "",
      ),
      responsibleOfficerName = "",
      responsibleOfficerPhoneNumber = null,
      responsibleOrganisationPhoneNumber = null,
      notifyingOrganisationEmail = "",
    )
  }
}
