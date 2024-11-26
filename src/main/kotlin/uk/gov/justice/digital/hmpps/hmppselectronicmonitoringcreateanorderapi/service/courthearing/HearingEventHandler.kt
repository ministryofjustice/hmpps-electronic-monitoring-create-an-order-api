package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Defendant
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.JudicialResultsPrompt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
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
  private val deadLetterQueueService: DeadLetterQueueService,
) {
  private val commentPlatformUsername = "COMMENT_PLATFORM"
  private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  companion object {
    //region order condition type
    const val ALCOHOL_ABSTAIN_MONITORING_UUID = "d54c3093-6b9b-4b61-80cf-a0bf4ed5d2e8"

    const val EXCLUSION_ZONE_UUID = "091cd45b-4312-476e-a122-18cc02fd1699"

    const val INCLUSION_ZONE_UUID = "9b216a08-4df8-41c2-a947-66506cd1e1b5"

    const val COMMUNITY_ORDER_CURFEW_UUID = "06b4c31d-1b3d-4850-b64c-4cad870b3a25"

    const val BAIL_ORDER_CURFEW = "629f6897-a46f-492e-9691-5226ee7810b7"

    const val BAIL_ORDER_EXCLUSION_NOT_ENTER_A_PLACE = "c1d490ed-1754-43b8-a485-fdab1a25f8cb"
    //endregion

    //region Common platform order types

    const val COMMUNITY_ORDER_ENGLAND_AND_WALES_UUID = "418b3aa7-65ab-4a4a-bab9-2f96b698118c"

    private const val COMMUNITY_ORDER_SCOTLAND_UUID = "ae617390-b41e-46ac-bd63-68a28512676a"

    const val ADULT_REMITTAL_FOR_SENTENCE_ON_CONDITIONAL_BAIL = "f917ba0c-1faf-4945-83a8-50be9049f9b4"

    const val CROWN_COURT_SENTENCE_IN_CUSTODY_WITH_BAIL_DIRECTION = "35430208-3705-44ce-b5d5-153c0337f6ab"

    // Suspended sentence order - detention in a young offender institution
    const val SSO_YOUNG_OFFENDER_INSTITUTION_DETENTION_UUID = "5679e5b7-0ca8-4d2a-ba80-7a50025fb589"

    const val SSO_INPRISONMENT_UUID = "8b1cff00-a456-40da-9ce4-f11c20959084"

    // end region
    fun isEnglandAdnWalesEMRequest(offence: Offence): Boolean {
      return !offence.judicialResults.any {
          judicialResults ->
        judicialResults.judicialResultTypeId== COMMUNITY_ORDER_SCOTLAND_UUID
      } &&
        offence.judicialResults.any {
            judicialResults ->
          judicialResults.judicialResultTypeId == ALCOHOL_ABSTAIN_MONITORING_UUID ||
            judicialResults.judicialResultTypeId == EXCLUSION_ZONE_UUID ||
            judicialResults.judicialResultTypeId == INCLUSION_ZONE_UUID ||
            judicialResults.judicialResultTypeId == COMMUNITY_ORDER_CURFEW_UUID ||
            judicialResults.judicialResultPrompts.any {
              it.judicialResultPromptTypeId == BAIL_ORDER_CURFEW ||
                it.judicialResultPromptTypeId == BAIL_ORDER_EXCLUSION_NOT_ENTER_A_PLACE
            }
        }
    }
  }

  fun handleHearingEvent(event: HearingEvent): List<String> {
    val result = mutableListOf<String>()
    val orders = getOrdersFromHearing(event.hearing)
    orders.forEach { order ->
      run {
        val submitResult = fmsService.submitOrder(order, FmsOrderSource.COMMENT_PLATFORM)
        // TODO log failed requests
        if (!submitResult.success) {
          val fullName = " ${order.deviceWearer!!.firstName} ${order.deviceWearer!!.lastName}"
          result.add(
            "Error create order for $fullName, error: ${submitResult.error} ",
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

  fun buildInterestedPartiesFromHearing(
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

  private fun getOrderForDefendant(hearing: Hearing, defendant: Defendant, offences: List<Offence>): Order {
    val judicialResults = offences.flatMap { it.judicialResults }.toList()

    val prompts = judicialResults.flatMap { it.judicialResultPrompts }.toList()
    val order = Order(username = commentPlatformUsername, status = OrderStatus.IN_PROGRESS)

    val monitoringConditions = MonitoringConditions(orderId = order.id)
    val orderedDate = judicialResults.first().orderedDate
    monitoringConditions.startDate = ZonedDateTime.of(orderedDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
    monitoringConditions.endDate = getPromptValue(prompts, "Until")?.let {
      val localDate = LocalDate.parse(it, formatter)
      ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
    }
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
    deviceWearer.gender = person?.gender?.name
    deviceWearer.adultAtTimeOfInstallation = !defendant.isYouth
    deviceWearer.sex = person?.gender?.name

    val address = person?.address
    if (address != null) {
      deviceWearer.noFixedAbode = false
      order.addresses.add(
        Address(
          orderId = order.id,
          addressType = AddressType.PRIMARY,
          addressLine1 = address.address1,
          addressLine2 = address.address2 ?: "",
          addressLine3 = address.address3 ?: "",
          addressLine4 = address.address4 ?: "N/A",
          postcode = address.postcode ?: "",
        ),
      )
    }
    order.deviceWearer = deviceWearer

    val contact = person?.contact
    if (contact != null) {
      val contactDetails =
        ContactDetails(orderId = order.id, contactNumber = contact.mobile ?: contact.home ?: contact.work)
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
    if (judicialResults.any { it.judicialResultTypeId == ALCOHOL_ABSTAIN_MONITORING_UUID }) {
      monitoringConditions.alcohol = true
      val alcoholConditions = AlcoholMonitoringConditions(
        orderId = order.id,
        monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
      )
      order.monitoringConditionsAlcohol = alcoholConditions
    }

    val exclusionZoneJudicialResult = judicialResults.firstOrNull { it.judicialResultTypeId == EXCLUSION_ZONE_UUID }
    if (exclusionZoneJudicialResult != null) {
      monitoringConditions.exclusionZone = true
      val zone = getEnforcementZone(order.id, exclusionZoneJudicialResult, prompts, EnforcementZoneType.EXCLUSION)
      monitoringConditions.startDate = zone.startDate
      monitoringConditions.endDate = zone.endDate
      order.enforcementZoneConditions.add(zone)
    }

    val inclusionZoneJudicialResults = judicialResults.firstOrNull { it.judicialResultTypeId == INCLUSION_ZONE_UUID }
    if (inclusionZoneJudicialResults != null) {
      monitoringConditions.exclusionZone = true
      val zone = getEnforcementZone(order.id, inclusionZoneJudicialResults, prompts, EnforcementZoneType.INCLUSION)
      monitoringConditions.startDate = zone.startDate
      monitoringConditions.endDate = zone.endDate
      order.enforcementZoneConditions.add(zone)
    }

    if (judicialResults.any { it.judicialResultTypeId == COMMUNITY_ORDER_CURFEW_UUID }) {
      monitoringConditions.curfew = true
      val condition = CurfewConditions(orderId = order.id)
      condition.startDate = getPromptValue(prompts, "Start date for tag")?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
      }
      condition.endDate = getPromptValue(prompts, "End date of tagging")?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
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
    judicialResults.firstOrNull {
      it.judicialResultPrompts.any { prompts -> prompts.judicialResultPromptTypeId == BAIL_ORDER_CURFEW }
    }?.let {
      monitoringConditions.curfew = true
      val condition = CurfewConditions(orderId = order.id)
      condition.startDate = ZonedDateTime.of(it.orderedDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
      order.curfewConditions = condition
    }

    judicialResults.firstOrNull {
      it.judicialResultPrompts.any { prompts ->
        prompts.judicialResultPromptTypeId == BAIL_ORDER_EXCLUSION_NOT_ENTER_A_PLACE
      }
    }?.let {
      monitoringConditions.exclusionZone = true
      val condition = EnforcementZoneConditions(orderId = order.id)
      condition.zoneType = EnforcementZoneType.EXCLUSION
      condition.zoneLocation = getPromptValue(prompts, "Exclusion - not to enter a place")
      condition.description = "Exclusion - not to enter a place"
      condition.startDate = ZonedDateTime.of(it.orderedDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
      order.enforcementZoneConditions.add(condition)
    }

    //region InterestedParties
    val responsibleOrganisation = getPromptValue(
      prompts,
      "Probation / YOT to be notified organisation name",
    ) ?: ""
    val responsibleOrganisationRegion = getPromptValue(
      prompts,
      "Probation / YOT to be notified organisation name",
    ) ?: ""
    val responsibleOrganisationEmail = getPromptValue(
      prompts,
      "Probation / YOT to be notified email address 1",
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

  private fun getEnforcementZone(
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
          it.judicialResultTypeId == SSO_INPRISONMENT_UUID
      }
    ) {
      return "Community"
    } else if (results.any { it.judicialResultTypeId == ADULT_REMITTAL_FOR_SENTENCE_ON_CONDITIONAL_BAIL }) {
      return "Pre-Trial"
    } else if (results.any { it.judicialResultTypeId == CROWN_COURT_SENTENCE_IN_CUSTODY_WITH_BAIL_DIRECTION }) {
      return "Post Release"
    }
    return null
  }

  private fun getConditionType(results: List<JudicialResults>): MonitoringConditionType? {
    if (results.any {
        it.judicialResultTypeId == COMMUNITY_ORDER_ENGLAND_AND_WALES_UUID ||
          it.judicialResultTypeId == SSO_YOUNG_OFFENDER_INSTITUTION_DETENTION_UUID ||
          it.judicialResultTypeId == SSO_INPRISONMENT_UUID
      }
    ) {
      return MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER
    } else if (results.any {
        it.judicialResultTypeId == ADULT_REMITTAL_FOR_SENTENCE_ON_CONDITIONAL_BAIL ||
          it.judicialResultTypeId == CROWN_COURT_SENTENCE_IN_CUSTODY_WITH_BAIL_DIRECTION
      }
    ) {
      return MonitoringConditionType.BAIL_ORDER
    }
    return null
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
}
