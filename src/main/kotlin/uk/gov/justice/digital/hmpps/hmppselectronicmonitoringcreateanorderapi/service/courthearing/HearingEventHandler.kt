package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing

import org.springframework.beans.factory.annotation.Value
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.JudicialResultsPrompt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.enums.BailOrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.enums.CommunityOrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
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
  @Value("\${settings.data-dictionary-version}") val defaultDataDictionaryVersion: String,
) {
  private val commentPlatformUsername = "COMMENT_PLATFORM"
  private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  companion object {

    //region Comment Platform UUIDs

    // Community order England / Wales(COEW)
    const val COEW = "418b3aa7-65ab-4a4a-bab9-2f96b698118c"

    // Youth Rehabilitation Order England and Wales (YROEW)
    const val YROEW = "73a4f6a2-b768-45de-beb7-3f4d2f933e11"

    // Youth rehabilitation order with fostering England / Wales (YROFEW)
    const val YROFEW = "ae8c21a9-cf2a-487b-8fae-58d50c7104f0"

    // Youth rehabilitation order with intensive supervision and surveillance England / Wales (YROISS)
    const val YROISS = "0b5ce679-b262-436d-8f94-aa78de85022a"

    // Suspended sentence order - detention in a young offender institution (SUSPSD)
    const val SUSPSD = "5679e5b7-0ca8-4d2a-ba80-7a50025fb589"

    // Suspended sentence order - imprisonment (SUSPS)
    const val SUSPS = "8b1cff00-a456-40da-9ce4-f11c20959084"

    // Supervision default order(SDO)
    const val SDO = "fd391847-f640-402e-a958-f33a014e6684"

    // Community order Scotland (COS)
    private const val COS = "ae617390-b41e-46ac-bd63-68a28512676a"

    // Bail Electronic Monitoring flag
    private const val BAIL_ELECTRONIC_MONITORING_FLAG = "86857bb0-aaa6-4a76-b226-812a9987fcb2"

    // Adult remittal for sentence on conditional bail (REMCB)
    const val REMCB = "f917ba0c-1faf-4945-83a8-50be9049f9b4"

    // Committed to Crown Court for sentence - in custody with bail direction (CCSIB)
    const val CCSIB = "35430208-3705-44ce-b5d5-153c0337f6ab"

    // Remand in care of Local Authority with bail direction (RILAB)
    const val RILAB = "f666fd58-36c5-493f-aa11-89714faee6e6"

    // Remanded in custody with bail direction (RIB)
    const val RIB = "e26940b7-2534-42f2-9c44-c70072bf6ad2"

    // Remanded on conditional bail (RC)
    const val RC = "3a529001-2f43-45ba-a0a8-d3ced7e9e7ad"

    // Remitted from the Crown Court to the Magistrates' Court in local authority accommodation with bail direction (RCCLAB)
    const val RCCLAB = "9fd1849f-f91f-4fa7-adfd-ef24f64654eb"

    // Sent to Crown Court in custody for trial with bail direction (CCIIB)
    const val CCIIB = "062373fb-ada8-49a1-b7de-659426ba6b88"

    // Sent to Crown Court for trial on conditional bail (CCIC)
    const val CCIC = "b318ca35-8b6a-41e5-a674-879ac9a05cc2"

    // Remitted from the Crown Court to the Magistrates' Court on conditional bail (RCCCB)
    const val RCCCB = "6266e4d8-a030-4ee7-be5c-9f5624f162e5"

    // Committed to Crown Court for sentence in Local Authority Accommodation (CCSILA)
    const val CCSILA = "61dc2dfb-df0a-4ea3-8821-4506cb51e7ec"

    // Youth remittal conditional bail (REMCBY)
    const val REMCBY = "0536dbd2-b922-4899-9bc9-cad08429a889"

    // Remand In care of Local Authority (RILA)
    const val RILA = "903b3e90-f185-40d3-92dd-6f81b73c4bb2"

    // Remitted from the Crown Court to the Magistrates' Court in local authority accommodation (RCCLA)
    const val RCCLA = "975f04f2-412b-40f4-8e9b-31edfefaea60"

    // Sent to Crown Court for trial in Local Authority Accommodation (CCIILA)
    const val CCIILA = "d4fa6715-b1b3-4145-bedc-061ccf33df50"

    // Youth remittal In Local Authority accommodation (REMIL)
    const val REMIL = "e85dc2ee-9b63-4dca-8dc0-7dedaa00f5bf"

    val BAIL_OR_REMAND_TO_CARE_CONDITION_UUIDs = arrayOf(
      REMCB,
      CCSIB,
      RILAB,
      RIB,
      RC,
      RCCLAB,
      CCIIB,
      CCIC,
      RCCCB,
      CCSILA,
      REMCBY,
      RILA,
      RCCLA,
      CCIILA,
      REMIL,
    )

    val COMMUNITY_ORDER_UUIDS = arrayOf(
      COEW,
      SUSPSD,
      SUSPS,
      YROEW,
      YROFEW,
      SDO,
      YROISS,
    )

    //endregion
    fun isEnglandAdnWalesEMRequest(offence: Offence): Boolean = !offence.judicialResults.any { judicialResults ->
      // If it's a Scottish court case
      judicialResults.judicialResultTypeId == COS
    } &&
      offence.judicialResults.any { judicialResults ->
        // If it's a known community order type
        CommunityOrderType.from(judicialResults.judicialResultTypeId) != null ||
          (
            BAIL_OR_REMAND_TO_CARE_CONDITION_UUIDs.contains(judicialResults.judicialResultTypeId) &&
              judicialResults.judicialResultPrompts.any {
                // If it's a known Bail/Remand in care case, or it has bail electronic monitoring flag
                BailOrderType.from(it.judicialResultPromptTypeId) != null ||
                  it.judicialResultPromptTypeId == BAIL_ELECTRONIC_MONITORING_FLAG
              }
            )
      }
  }

  fun handleHearingEvent(event: HearingEvent): List<String> {
    val result = mutableListOf<String>()
    val orders = getOrdersFromHearing(event.hearing)
    val startTimeInMs = System.currentTimeMillis()
    val startDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"))
    orders.forEach { order ->
      run {
        val submitResult = fmsService.submitOrder(order, FmsOrderSource.COMMON_PLATFORM)

        if (!submitResult.success) {
          val fullName = " ${order.deviceWearer!!.firstName} ${order.deviceWearer!!.lastName}"
          result.add("Error create order for $fullName, error: ${submitResult.error} ")
          eventService.recordEvent(
            "Common_Platform_Failed_Request",
            mapOf(
              "Error" to submitResult.error,
              "Start Date And Time" to startDateTime.format(formatter),
            ),
            System.currentTimeMillis() - startTimeInMs,
          )
        } else {
          eventService.recordEvent(
            "Common_Platform_Success_Request",
            mapOf(
              "OrderType" to order.monitoringConditions!!.orderType!!.value,
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
    val dataDictionaryVersion = DataDictionaryVersion.entries.first { it.name == defaultDataDictionaryVersion }
    val order = Order(
      versions = mutableListOf(
        OrderVersion(
          username = commentPlatformUsername,
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          orderId = UUID.randomUUID(),
          dataDictionaryVersion = dataDictionaryVersion,
        ),
      ),
    )

    val monitoringConditions = MonitoringConditions(versionId = order.getCurrentVersion().id)
    val orderedDate = judicialResults.first().orderedDate
    monitoringConditions.startDate = ZonedDateTime.of(orderedDate, LocalTime.MIDNIGHT, ZoneId.of("Europe/London"))

    monitoringConditions.conditionType = getConditionType(judicialResults)
    monitoringConditions.orderType = getOrderType(judicialResults)

    if (monitoringConditions.conditionType == MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER) {
      loadCommunityOrderConditions(judicialResults, order, monitoringConditions, prompts, hearing)
    } else if (monitoringConditions.conditionType == MonitoringConditionType.BAIL_ORDER) {
      loadBailOrderConditions(judicialResults, order, monitoringConditions, prompts, hearing)
    }

    order.monitoringConditions = monitoringConditions

    val person = defendant.personDefendant?.personDetails
    val deviceWearer = DeviceWearer(versionId = order.getCurrentVersion().id)

    if (person?.dateOfBirth != null) {
      deviceWearer.dateOfBirth = ZonedDateTime.of(person.dateOfBirth, LocalTime.MIDNIGHT, ZoneId.of("Europe/London"))
    }
    deviceWearer.firstName = person?.firstName
    deviceWearer.lastName = person?.lastName
    deviceWearer.sex = getSex(person?.gender)
    deviceWearer.adultAtTimeOfInstallation = !defendant.isYouth

    val address = person?.address
    if (address != null) {
      deviceWearer.noFixedAbode = false
      order.addresses.add(
        Address(
          versionId = order.getCurrentVersion().id,
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

    val contactDetails =
      ContactDetails(
        versionId = order.getCurrentVersion().id,
        contactNumber = contact?.mobile ?: contact?.home ?: contact?.work ?: "",
      )
    order.contactDetails = contactDetails

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
        versionId = order.getCurrentVersion().id,
        monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
        startDate = monitoringConditions.startDate,
        endDate = getPromptValue(prompts, "Until")?.let {
          val localDate = LocalDate.parse(it, formatter)
          ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.of("Europe/London"))
        },
      )

      monitoringConditions.endDate = alcoholConditions.endDate
      order.monitoringConditionsAlcohol = alcoholConditions
    }

    val exclusionZoneJudicialResult = judicialResults.firstOrNull {
      it.judicialResultTypeId == CommunityOrderType.EXCLUSION_ZONE.uuid ||
        it.judicialResultTypeId == CommunityOrderType.YOUTH_EXCLUSION.uuid
    }
    if (exclusionZoneJudicialResult != null) {
      monitoringConditions.exclusionZone = true
      val zone =
        getCommunityOrderEnforcementZone(
          order.getCurrentVersion().id,
          exclusionZoneJudicialResult,
          prompts,
          EnforcementZoneType.EXCLUSION,
        )
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
        getCommunityOrderEnforcementZone(
          order.getCurrentVersion().id,
          inclusionZoneJudicialResults,
          prompts,
          EnforcementZoneType.INCLUSION,
        )
      monitoringConditions.startDate = zone.startDate
      monitoringConditions.endDate = zone.endDate
      order.enforcementZoneConditions.add(zone)
    }

    if (judicialResults.any {
        it.judicialResultTypeId == CommunityOrderType.TRAIL_MONITORING.uuid ||
          it.judicialResultTypeId == CommunityOrderType.YOUTH_TRAIL.uuid
      }
    ) {
      monitoringConditions.trail = true
      val trailCondition = TrailMonitoringConditions(versionId = order.getCurrentVersion().id)
      val startTime = getPromptValue(prompts, "Start time of tagging") ?: "00:00"

      val startDate = getPromptValue(
        prompts,
        "The defendant's whereabouts are to be electronically monitored. Start date",
      )?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.parse(startTime), ZoneId.of("Europe/London"))
      }

      trailCondition.startDate = startDate
      monitoringConditions.startDate = startDate
      val endTime = getPromptValue(prompts, "End time of tagging") ?: "00:00"
      val endDate =
        getPromptValue(prompts, "End date of tagging")?.let {
          val localDate = LocalDate.parse(it, formatter)
          ZonedDateTime.of(localDate, LocalTime.parse(endTime), ZoneId.of("Europe/London"))
        }
      trailCondition.endDate = endDate
      monitoringConditions.endDate = endDate
      order.monitoringConditionsTrail = trailCondition
    }

    if (judicialResults.any {
        it.judicialResultTypeId == CommunityOrderType.COMMUNITY_ORDER_CURFEW.uuid ||
          it.judicialResultTypeId == CommunityOrderType.YOUTH_CURFEW.uuid ||
          it.judicialResultTypeId == CommunityOrderType.SUPERVISION_CURFEW.uuid
      }
    ) {
      monitoringConditions.curfew = true
      val condition = CurfewConditions(versionId = order.getCurrentVersion().id)
      val startTime = getPromptValue(prompts, "Start time of tagging") ?: "00:00"
      condition.startDate = getPromptValue(prompts, "Start date of tagging")?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.parse(startTime), ZoneId.of("Europe/London"))
      }
      val endTime = getPromptValue(prompts, "End time of tagging") ?: "00:00"
      condition.endDate = getPromptValue(prompts, "End date of tagging")?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.parse(endTime), ZoneId.of("Europe/London"))
      }
      val defendantRemainAt = getPromptValue(prompts, "Defendant to remain at") ?: ""
      val detailsAndTiming = getPromptValue(prompts, "Details and timings") ?: ""
      condition.curfewDescription = "$defendantRemainAt $detailsAndTiming"
      order.curfewConditions = condition
      monitoringConditions.endDate = condition.endDate
    }

    //region InterestedParties
    val responsibleOrganisation = getResponsibleOrganisation(
      getPromptValue(
        prompts,
        "Responsible officer",
      ),
    )
    var responsibleOrganisationRegion = getPromptValue(
      prompts,
      "Probation team to be notified organisation name",
    ) ?: ""
    var responsibleOrganisationEmail = getPromptValue(
      prompts,
      "Probation team to be notified email address 1",
    ) ?: ""

    if (responsibleOrganisation == "YJS") {
      responsibleOrganisationRegion = getPromptValue(
        prompts,
        "Youth offending team to be notified organisation name",
      ) ?: ""
      responsibleOrganisationEmail = getPromptValue(
        prompts,
        "Youth offending team to be notified email address 1",
      ) ?: ""
    }

    order.interestedParties = buildInterestedPartiesFromHearing(
      order.getCurrentVersion().id,
      responsibleOrganisation,
      responsibleOrganisationRegion,
      responsibleOrganisationEmail,
      hearing.jurisdictionType.value,
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
      it.judicialResultPrompts.any { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.CURFEW.uuid ||
          BailOrderType.REMAND_TO_CARE_CURFEWS.contains(prompts.judicialResultPromptTypeId)
      }
    }?.let {
      val conditionPrompt = it.judicialResultPrompts.first { prompts ->
        prompts.judicialResultPromptTypeId == BailOrderType.CURFEW.uuid ||
          BailOrderType.REMAND_TO_CARE_CURFEWS.contains(prompts.judicialResultPromptTypeId)
      }
      monitoringConditions.curfew = true
      val condition = CurfewConditions(versionId = order.getCurrentVersion().id)
      condition.startDate = ZonedDateTime.of(it.orderedDate, LocalTime.MIDNIGHT, ZoneId.of("Europe/London"))
      condition.endDate = monitoringConditions.endDate
      condition.curfewDescription = conditionPrompt.value
      order.curfewConditions = condition
    }

    BailOrderType.ENFORCEMENT_ZONE_IDS.forEach { bailOrderTypeZoneTypeEntry ->
      judicialResults.firstOrNull {
        it.judicialResultPrompts.any { prompts ->
          prompts.judicialResultPromptTypeId == bailOrderTypeZoneTypeEntry.key.uuid
        }
      }?.let {
        val conditionPrompt = it.judicialResultPrompts.first { prompts ->
          prompts.judicialResultPromptTypeId == bailOrderTypeZoneTypeEntry.key.uuid
        }
        monitoringConditions.exclusionZone = true
        val condition =
          getBailOrderEnforcementZone(
            conditionPrompt,
            it.orderedDate,
            bailOrderTypeZoneTypeEntry.value,
            order.getCurrentVersion().id,
          )
        condition.endDate = monitoringConditions.endDate
        order.enforcementZoneConditions.add(condition)
      }
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
      order.getCurrentVersion().id,
      responsibleOrganisation,
      responsibleOrganisationRegion,
      responsibleOrganisationEmail,
      hearing.jurisdictionType.value,
      hearing.courtCentre.name,
    )
    //endregion
  }

  private fun getNextCourtHearingDate(prompts: List<JudicialResultsPrompt>): ZonedDateTime? {
    var nextHearingDetails = ""
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
    var localDate: LocalDate? = null
    if (nextHearingDetailsAsMap.containsKey(nextHearingDateKey)) {
      localDate = LocalDate.parse(nextHearingDetailsAsMap[nextHearingDateKey]!!, formatter)
    } else if (nextHearingDetailsAsMap.containsKey("Week Commencing")) {
      localDate = LocalDate.parse(nextHearingDetailsAsMap["Week Commencing"]!!, formatter)
    }

    if (localDate == null) {
      return null
    }

    return ZonedDateTime.of(
      localDate,
      LocalTime.parse(nextHearingDetailsAsMap["Time of hearing"] ?: ""),
      ZoneId.of("Europe/London"),
    )
  }

  private fun getBailOrderEnforcementZone(
    conditionPrompt: JudicialResultsPrompt,
    startDate: LocalDate?,
    zoneType: EnforcementZoneType,
    versionId: UUID,
  ): EnforcementZoneConditions {
    val condition = EnforcementZoneConditions(versionId = versionId)
    condition.zoneType = zoneType

    condition.description = "${conditionPrompt.label} ${conditionPrompt.value}"
    condition.startDate = ZonedDateTime.of(startDate, LocalTime.MIDNIGHT, ZoneId.of("Europe/London"))
    return condition
  }

  private fun getCommunityOrderEnforcementZone(
    versionId: UUID,
    judicialResult: JudicialResults,
    prompts: List<JudicialResultsPrompt>,
    zoneType: EnforcementZoneType,
  ): EnforcementZoneConditions {
    val zone = EnforcementZoneConditions(versionId = versionId)
    zone.zoneType = zoneType
    val startTime = getPromptValue(prompts, "Start time for tag") ?: "00:00"

    val startDate = getPromptValue(prompts, "Start date for tag")?.let {
      val localDate = LocalDate.parse(it, formatter)
      ZonedDateTime.of(localDate, LocalTime.parse(startTime), ZoneId.of("Europe/London"))
    }

    zone.startDate = startDate
    val endTime = getPromptValue(prompts, "End time for tag") ?: "00:00"
    val endDate =
      getPromptValue(prompts, "End date for tag")?.let {
        val localDate = LocalDate.parse(it, formatter)
        ZonedDateTime.of(localDate, LocalTime.parse(endTime), ZoneId.of("Europe/London"))
      }

    zone.endDate = endDate
    zone.duration = getPromptValue(prompts, "Exclusion and electronic monitoring period")
    zone.zoneLocation = getPromptValue(prompts, "Place / area")
    zone.description = judicialResult.resultText

    return zone
  }

  private fun getOrderType(results: List<JudicialResults>): OrderType? {
    if (results.any {
        COMMUNITY_ORDER_UUIDS.contains(it.judicialResultTypeId)
      }
    ) {
      return OrderType.COMMUNITY
    } else if (results.any {
        BAIL_OR_REMAND_TO_CARE_CONDITION_UUIDs.contains(it.judicialResultTypeId)
      }
    ) {
      return OrderType.PRE_TRIAL
    }
    return null
  }

  private fun getConditionType(results: List<JudicialResults>): MonitoringConditionType? {
    if (results.any { COMMUNITY_ORDER_UUIDS.contains(it.judicialResultTypeId) }) {
      return MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER
    } else if (results.any { BAIL_OR_REMAND_TO_CARE_CONDITION_UUIDs.contains(it.judicialResultTypeId) }) {
      return MonitoringConditionType.BAIL_ORDER
    }

    return null
  }

  private fun getSex(gender: Gender?): String = when (gender) {
    Gender.MALE -> "male"
    Gender.FEMALE -> "female"
    Gender.NOT_KNOWN -> "unknown"
    Gender.NOT_SPECIFIED -> "prefer not to say"
    null -> ""
  }

  private fun getResponsibleOrganisation(responsibleOfficer: String?): String = when (responsibleOfficer) {
    "an officer of a provider of probation services" -> "Probation"
    "a probation officer" -> "Probation"
    "a member of the youth offending team" -> "YJS"
    "the electronic monitoring supervisor" -> "Field Monitoring Service"
    "the officer in charge of the Attendance Centre" -> "Policy/youth attendance"
    else -> ""
  }

  private fun getPromptValue(prompts: List<JudicialResultsPrompt>, label: String): String? = prompts.firstOrNull {
    it.label == label
  }?.value

  private fun buildInterestedPartiesFromHearing(
    versionId: UUID,
    responsibleOfficer: String,
    responsibleOrganisationRegion: String,
    responsibleOrganisationId: String,
    notifyingOrganisation: String,
    notifyingOrganisationName: String,
  ): InterestedParties = InterestedParties(
    versionId = versionId,
    notifyingOrganisation = notifyingOrganisation,
    notifyingOrganisationName = notifyingOrganisationName,
    notifyingOrganisationEmail = "",
    responsibleOrganisation = responsibleOfficer,
    responsibleOrganisationRegion = responsibleOrganisationRegion,
    responsibleOrganisationEmail = responsibleOrganisationId,
    responsibleOfficerName = "",
    responsibleOfficerPhoneNumber = null,
  )
}
