package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.util.UUID

data class FmsRetrieveDWandMO(
  @JsonProperty("case_id")
  val caseId: String,
  @JsonProperty("device_wearer")
  val deviceWearer: DeviceWearer,
  @JsonProperty("monitoring_order")
  val monitoringOrder: MonitoringOrder,
) {
  fun toOrderVersion(
    orderId: UUID,
    username: String,
    status: OrderStatus,
    type: RequestType,
    dataDictionaryVersion: DataDictionaryVersion,
  ): OrderVersion {
    val version = OrderVersion(
      orderId = orderId,
      username = username,
      status = status,
      type = type,
      dataDictionaryVersion = dataDictionaryVersion,
    )
    val versionId = version.id

    version.deviceWearer = deviceWearer.toDeviceWearer(versionId).apply {
      courtCaseReferenceNumber = monitoringOrder.toCourtCaseReferenceNumber()
    }
    version.deviceWearerResponsibleAdult = deviceWearer.toResponsibleAdult(versionId)
    version.mappa = deviceWearer.toMappa(versionId)
    version.addresses = listOfNotNull(
      deviceWearer.toAddress(versionId),
      deviceWearer.toSecondaryAddress(versionId),
      deviceWearer.toTertiaryAddress(versionId),
    ).toMutableList()

    version.contactDetails = deviceWearer.toContactDetails(versionId)

    version.installationAndRisk = deviceWearer.toInstallationAndRisk(versionId)

    version.interestedParties = monitoringOrder.toInterestedParties(versionId)
    version.probationDeliveryUnit = monitoringOrder.toProbationDeliveryUnit(versionId)
    version.monitoringConditions = monitoringOrder.toMonitoringConditions(versionId)
    version.monitoringConditionsTrail = monitoringOrder.toTrailMonitoringConditions(versionId)
    version.monitoringConditionsAlcohol = monitoringOrder.toAlcoholMonitoringConditions(versionId)
    version.offences = monitoringOrder.toOffences(versionId).toMutableList()
    version.dapoClauses = monitoringOrder.toDapo(versionId).toMutableList()
    version.enforcementZoneConditions = monitoringOrder.toEnforcementZoneConditions(versionId).toMutableList()
    version.curfewConditions = monitoringOrder.toCurfewConditions(versionId)
    version.curfewReleaseDateConditions = monitoringOrder.toCurfewReleaseDateConditions(versionId)
    version.curfewTimeTable = monitoringOrder.toCurfewTimeTable(versionId).toMutableList()
    version.variationDetails = monitoringOrder.toVariationDetails(versionId)
    version.installationAppointment = monitoringOrder.toInstallationAppointment(versionId)

    version.monitoringConditions?.startDate = version.getMonitoringStartDate()
    version.monitoringConditions?.endDate = version.getMonitoringEndDate()

    return version
  }
}
