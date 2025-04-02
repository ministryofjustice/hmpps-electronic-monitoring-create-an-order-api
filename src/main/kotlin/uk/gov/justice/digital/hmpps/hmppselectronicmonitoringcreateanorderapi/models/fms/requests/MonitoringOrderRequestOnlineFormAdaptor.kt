package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MagistrateCourt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthJusticeServiceRegions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.CurfewSchedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.Schedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.Zone
import java.time.format.DateTimeFormatter

class MonitoringOrderRequestOnlineFormAdaptor(private val order: Order, private val deviceWearerId: String) :
  MonitoringOrderRequest() {

  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  override val abstinence: String
    get() {
      val monitoringType = order.monitoringConditionsAlcohol?.monitoringType

      if (monitoringType === AlcoholMonitoringType.ALCOHOL_ABSTINENCE) {
        return "Yes"
      }

      if (monitoringType == AlcoholMonitoringType.ALCOHOL_LEVEL) {
        return "No"
      }

      return super.abstinence
    }

  override val caseId: String
    get() {
      return deviceWearerId
    }

  override val conditionalReleaseDate: String?
    get() {
      if (order.monitoringConditions?.curfew == true) {
        return order.curfewReleaseDateConditions?.releaseDate?.format(dateFormatter)
      }
      return super.conditionalReleaseDate
    }

  override val conditionType: String
    get() {
      return order.monitoringConditions?.conditionType?.value ?: super.conditionType
    }

  override val curfewDescription: String
    get() {
      return order.curfewConditions?.curfewDescription ?: super.curfewDescription
    }

  override val curfewDuration: MutableList<CurfewSchedule>
    get() {
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

  override val curfewEnd: String
    get() {
      return order.curfewConditions?.endDate?.format(dateTimeFormatter) ?: super.curfewEnd
    }

  override val curfewStart: String
    get() {
      return order.curfewConditions?.startDate?.format(dateTimeFormatter) ?: super.curfewStart
    }

  override val deviceWearer: String
    get() {
      if (order.deviceWearer !== null) {
        return "${order.deviceWearer!!.firstName} ${order.deviceWearer!!.lastName}"
      }
      return super.deviceWearer
    }

  override val enforceableCondition: MutableList<EnforceableCondition>
    get() {
      val enforceableConditions = mutableListOf<EnforceableCondition>()
      val conditions = order.monitoringConditions

      if (conditions === null) {
        return enforceableConditions
      }

      if (conditions.curfew == true) {
        val curfew = order.curfewConditions
        enforceableConditions.add(
          EnforceableCondition(
            "Curfew with EM",
            startDate = curfew?.startDate?.format(dateTimeFormatter) ?: "",
            endDate = curfew?.endDate?.format(dateTimeFormatter) ?: "",
          ),
        )
      }

      if (conditions.trail == true) {
        val trail = order.monitoringConditionsTrail
        enforceableConditions.add(
          EnforceableCondition(
            "Location Monitoring (Fitted Device)",
            startDate = trail?.startDate?.format(dateTimeFormatter) ?: "",
            endDate = trail?.endDate?.format(dateTimeFormatter) ?: "",
          ),
        )
      }

      if (conditions.exclusionZone == true) {
        enforceableConditions.add(
          EnforceableCondition(
            "EM Exclusion / Inclusion Zone",
            startDate = conditions.startDate?.format(dateTimeFormatter) ?: "",
            endDate = conditions.endDate?.format(dateTimeFormatter) ?: "",
          ),
        )
      }

      if (conditions.alcohol == true) {
        val alcoholConditions = order.monitoringConditionsAlcohol

        if (alcoholConditions?.monitoringType == AlcoholMonitoringType.ALCOHOL_ABSTINENCE) {
          enforceableConditions.add(
            EnforceableCondition(
              "AAMR",
              startDate = alcoholConditions.startDate?.format(dateTimeFormatter) ?: "",
              endDate = alcoholConditions.endDate?.format(dateTimeFormatter) ?: "",
            ),
          )
        }

        if (alcoholConditions?.monitoringType == AlcoholMonitoringType.ALCOHOL_LEVEL) {
          enforceableConditions.add(
            EnforceableCondition(
              "AML",
              startDate = alcoholConditions.startDate?.format(dateTimeFormatter) ?: "",
              endDate = alcoholConditions.endDate?.format(dateTimeFormatter) ?: "",
            ),
          )
        }
      }

      return enforceableConditions
    }

  override val exclusionZones: List<Zone>
    get() {
      return order.enforcementZoneConditions
        .filter {
          it.zoneType == EnforcementZoneType.EXCLUSION
        }
        .map {
          Zone(
            description = it.description,
            duration = it.duration,
            start = it.startDate?.format(dateFormatter) ?: "",
            end = it.endDate?.format(dateFormatter) ?: "",
          )
        }
    }

  override val hdc: String
    get() {
      if (order.monitoringConditions?.hdc == YesNoUnknown.YES) {
        return "Yes"
      }
      return "No"
    }

  override val inclusionZones: List<Zone>
    get() {
      return order.enforcementZoneConditions
        .filter {
          it.zoneType == EnforcementZoneType.INCLUSION
        }
        .map {
          Zone(
            description = it.description,
            duration = it.duration,
            start = it.startDate?.format(dateFormatter) ?: "",
            end = it.endDate?.format(dateFormatter) ?: "",
          )
        }
    }

  override val installationAddress1: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.INSTALLATION
      }?.addressLine1 ?: super.installationAddress1
    }

  override val installationAddress2: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.INSTALLATION
      }?.addressLine2 ?: super.installationAddress2
    }

  override val installationAddress3: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.INSTALLATION
      }?.addressLine3 ?: super.installationAddress3
    }

  override val installationAddress4: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.INSTALLATION
      }?.addressLine4 ?: super.installationAddress4
    }

  override val installationAddressPostcode: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.INSTALLATION
      }?.postcode ?: super.installationAddressPostcode
    }

  override val issp: String
    get() {
      if (order.monitoringConditions?.issp == YesNoUnknown.YES) {
        return "Yes"
      }
      return "No"
    }

  override val noEmail: String
    get() {
      return order.interestedParties?.notifyingOrganisationEmail ?: super.noEmail
    }

  override val noName: String
    get() {
      return Prison.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: CrownCourt.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: MagistrateCourt.from(order.interestedParties?.notifyingOrganisationName)?.value
        ?: order.interestedParties?.notifyingOrganisationName
        ?: super.noName
    }

  override val notifyingOrganization: String
    get() {
      return NotifyingOrganisation.from(order.interestedParties?.notifyingOrganisation)?.value
        ?: order.interestedParties?.notifyingOrganisation
        ?: super.notifyingOrganization
    }

  override val offence: String?
    get() {
      return Offence.from(order.installationAndRisk?.offence)?.value
        ?: order.installationAndRisk?.offence
        ?: super.offence
    }

  override val orderEnd: String
    get() {
      return order.monitoringConditions?.endDate?.format(dateTimeFormatter) ?: super.orderEnd
    }

  override val orderId: String
    get() {
      return order.id.toString()
    }

  override val orderRequestType: String
    get() {
      return order.type.value
    }

  override val orderStart: String
    get() {
      return order.monitoringConditions?.startDate?.format(dateTimeFormatter) ?: super.orderStart
    }

  override val orderType: String
    get() {
      return order.monitoringConditions?.orderType?.value ?: super.orderType
    }

  override val orderTypeDescription: String?
    get() {
      return order.monitoringConditions?.orderTypeDescription?.value ?: super.orderTypeDescription
    }

  override val orderVariationDate: String
    get() {
      return order.variationDetails?.variationDate?.format(dateTimeFormatter) ?: super.orderVariationDate
    }

  override val orderVariationType: String
    get() {
      return order.variationDetails?.variationType?.value ?: super.orderVariationType
    }

  override val responsibleOfficerName: String
    get() {
      return order.interestedParties?.responsibleOfficerName ?: super.responsibleOfficerName
    }

  override val responsibleOfficerPhone: String?
    get() {
      return order.interestedParties?.responsibleOfficerPhoneNumber ?: super.responsibleOfficerPhone
    }

  override val responsibleOrganization: String
    get() {
      return ResponsibleOrganisation.from(order.interestedParties?.responsibleOrganisation)?.value
        ?: order.interestedParties?.responsibleOrganisation
        ?: super.responsibleOrganization
    }

  override val roAddress1: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.RESPONSIBLE_ORGANISATION
      }?.addressLine1 ?: super.roAddress1
    }

  override val roAddress2: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.RESPONSIBLE_ORGANISATION
      }?.addressLine2 ?: super.roAddress2
    }

  override val roAddress3: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.RESPONSIBLE_ORGANISATION
      }?.addressLine3 ?: super.roAddress3
    }

  override val roAddress4: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.RESPONSIBLE_ORGANISATION
      }?.addressLine4 ?: super.roAddress4
    }

  override val roEmail: String
    get() {
      return order.interestedParties?.responsibleOrganisationEmail ?: super.roEmail
    }

  override val roPhone: String?
    get() {
      return order.interestedParties?.responsibleOrganisationPhoneNumber ?: super.roPhone
    }

  override val roPostCode: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.RESPONSIBLE_ORGANISATION
      }?.postcode ?: super.roPostCode
    }

  override val roRegion: String
    get() {
      return ProbationServiceRegion.from(order.interestedParties?.responsibleOrganisationRegion)?.value
        ?: YouthJusticeServiceRegions.from(order.interestedParties?.responsibleOrganisationRegion)?.value
        ?: order.interestedParties?.responsibleOrganisationRegion
        ?: super.roRegion
    }

  override val sentenceType: String
    get() {
      return order.monitoringConditions?.sentenceType?.value ?: super.sentenceType
    }

  override val serviceEndDate: String
    get() {
      return order.monitoringConditions?.endDate?.format(dateFormatter) ?: super.serviceEndDate
    }

  override val tagAtSourceDetails: String
    get() {
      val alcoholConditions = order.monitoringConditionsAlcohol

      if (alcoholConditions != null) {
        val prisonName = alcoholConditions.prisonName
        if (!prisonName.isNullOrBlank()) {
          return prisonName
        }

        val probationOfficeName = alcoholConditions.probationOfficeName
        if (!probationOfficeName.isNullOrBlank()) {
          return probationOfficeName
        }
      }

      return super.tagAtSourceDetails
    }

  override val trailMonitoring: String
    get() {
      var trailMonitoring = super.trailMonitoring

      if (order.monitoringConditions?.trail == true) {
        trailMonitoring = "Yes"
      }

      if (order.monitoringConditions?.exclusionZone == true) {
        trailMonitoring = "No"
      }

      return trailMonitoring
    }
}
