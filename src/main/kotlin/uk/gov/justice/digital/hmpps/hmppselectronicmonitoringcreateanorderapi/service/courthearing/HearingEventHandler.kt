package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Defendant
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Hearing
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.HearingEvent
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.JudicialResults
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.JurisdictionType
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.JudicialResultsPrompt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OrderService
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class HearingEventHandler(
  private val orderRepository: OrderRepository,
  private val orderService: OrderService,

) {
  private val commentPlatformUsername = "COMMENT_PLATFORM"
  private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  companion object {
    const val ALCOHOL_ABSTAIN_MONITORING_UUID = "d54c3093-6b9b-4b61-80cf-a0bf4ed5d2e8"

    fun isTaggableOffence(offence: Offence): Boolean {
      return offence.judicialResults.any {
          judicialResults ->
        // Community order England_Wales, Alcohol abstinence and monitoring
        judicialResults.judicialResultTypeId == ALCOHOL_ABSTAIN_MONITORING_UUID
      }
    }
  }

  fun handleHearingEvent(event: HearingEvent) {
    val orders = getOrdersFromHearing(event.hearing)
    orders.forEach { order -> orderService.submitOrder(order) }
  }

  fun getOrdersFromHearing(hearing: Hearing): List<Order> {
    // Get defendant that has taggable offences
    val defendantOffences = hearing.prosecutionCases
      .flatMap { it.defendants }
      .filter { defendant -> defendant.offences.any { isTaggableOffence(it) } }
      .groupBy { it }
      .mapValues { (_, defendants) ->
        defendants.flatMap { it.offences }.filter { isTaggableOffence(it) }.toMutableList()
      }.toMap()

    // map each defendant to Order
    return defendantOffences.map { (defendant, offences) ->
      getOrderForDefendant(hearing, defendant, offences)
    }
  }

  private fun getOrderForDefendant(hearing: Hearing, defendant: Defendant, offences: List<Offence>): Order {
    val judicialResults = offences.flatMap { it.judicialResults }.toList()

    val prompts = judicialResults.flatMap { it.judicialResultPrompts }.toList()
    val order = Order(username = commentPlatformUsername, status = OrderStatus.IN_PROGRESS)

    val monitoringConditions = MonitoringConditions(orderId = order.id)
    val orderedDate = judicialResults.first().orderedDate
    monitoringConditions.startDate = ZonedDateTime.of(orderedDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
    monitoringConditions.endDate = getPromptValue(prompts, "End Date")?.let {
      val localDate = LocalDate.parse(it, formatter)
      ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
    }
    monitoringConditions.conditionType = getConditionType(judicialResults)
    monitoringConditions.orderType = getOrderType(judicialResults)

    if (judicialResults.any { it.judicialResultTypeId == ALCOHOL_ABSTAIN_MONITORING_UUID }) {
      monitoringConditions.alcohol = true
      val alcoholConditions = AlcoholMonitoringConditions(
        orderId = order.id,
        monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
      )
      order.monitoringConditionsAlcohol = alcoholConditions
    }

    order.monitoringConditions = monitoringConditions
    val responsibleOfficer = ResponsibleOfficer(orderId = order.id)
    if (hearing.jurisdictionType == JurisdictionType.MAGISTRATES) {
      responsibleOfficer.notifyingOrganisation = "Magistrates Court"
    } else if (hearing.jurisdictionType == JurisdictionType.CROWN) {
      responsibleOfficer.notifyingOrganisation = "Crown Court"
    }
    val organisation = getPromptValue(prompts, "Responsible officer")
    responsibleOfficer.organisation = getResponsibleOrganisation(organisation)
    responsibleOfficer.organisationRegion = getPromptValue(prompts, "Probation team to be notified organisation name")
    responsibleOfficer.organisationEmail = getPromptValue(prompts, "Probation team to be notified email address 1")
    responsibleOfficer.organisationPostCode = hearing.courtCentre.address?.postcode
    order.responsibleOfficer = responsibleOfficer

    val person = defendant.personDefendant?.personDetails
    val deviceWearer = DeviceWearer(orderId = order.id)

    deviceWearer.dateOfBirth = ZonedDateTime.of(person?.dateOfBirth, LocalTime.MIDNIGHT, ZoneId.of("GMT"))
    deviceWearer.firstName = person?.firstName
    deviceWearer.lastName = person?.lastName
    deviceWearer.gender = person?.gender?.name
    deviceWearer.adultAtTimeOfInstallation = !defendant.isYouth
    deviceWearer.sex = person?.gender?.name

    order.deviceWearer = deviceWearer
    val address = person?.address
    if (address != null) {
      val primaryAddress = Address(
        orderId = order.id,
        addressType = AddressType.PRIMARY,
        addressLine1 = address.address1,
        addressLine2 = address.address2 ?: "",
        addressLine3 = address.address3 ?: "",
        addressLine4 = address.address4 ?: "N/A",
        postcode = address.postcode ?: "",
      )
      order.addresses = mutableListOf(primaryAddress)
    }
    val contact = person?.contact
    if (contact != null) {
      val contactDetails =
        ContactDetails(orderId = order.id, contactNumber = contact.home ?: contact.mobile ?: contact.work)
      order.deviceWearerContactDetails = contactDetails
    }
    return order
  }

  private fun getOrderType(results: List<JudicialResults>): String? {
    if (results.any { it.resultText?.contains("COEW") == true }) {
      return "Community"
    }

    return null
  }
  private fun getConditionType(results: List<JudicialResults>): MonitoringConditionType? {
    if (results.any { it.resultText?.contains("COEW") == true }) {
      return MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER
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
