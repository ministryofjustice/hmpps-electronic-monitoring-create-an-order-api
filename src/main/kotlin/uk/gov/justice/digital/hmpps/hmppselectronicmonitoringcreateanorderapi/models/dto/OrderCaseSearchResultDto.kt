package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import java.util.UUID

data class OrderCaseSearchResultDto(val id: UUID, val versions: List<OrderVersionDto>)

fun OrderVersion.toOrderVersionDto(): OrderVersionDto {
  val dto = OrderVersionDto(
    id = id,
    versionId = versionId,
    additionalDocuments = additionalDocuments,
    addresses = addresses,
    contactDetails = contactDetails,
    curfewConditions = curfewConditions,
    curfewReleaseDateConditions = curfewReleaseDateConditions,
    curfewTimeTable = curfewTimeTable,
    deviceWearer = deviceWearer,
    deviceWearerResponsibleAdult = deviceWearerResponsibleAdult,
    enforcementZoneConditions = enforcementZoneConditions,
    fmsResultId = fmsResultId,
    fmsResultDate = fmsResultDate,
    installationAndRisk = installationAndRisk,
    interestedParties = interestedParties,
    probationDeliveryUnit = probationDeliveryUnit,
    isValid = isValid,
    mandatoryAttendanceConditions = mandatoryAttendanceConditions,
    monitoringConditions = monitoringConditions,
    monitoringConditionsAlcohol = monitoringConditionsAlcohol,
    monitoringConditionsTrail = monitoringConditionsTrail,
    status = status,
    type = type,
    username = username,
    submittedBy = submittedBy,
    variationDetails = variationDetails,
    installationLocation = installationLocation,
    installationAppointment = installationAppointment,
    dataDictionaryVersion = dataDictionaryVersion,
    orderParameters = orderParameters,
    dapoClauses = dapoClauses,
    offences = offences,
    offenceAdditionalDetails = offenceAdditionalDetails,
    mappa = mappa,
    detailsOfInstallation = detailsOfInstallation,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedDateTime = lastUpdatedDateTime,
    ownerCohort = ownerCohort,
    isSentencingAct = isSentencingAct,
  )
  dto.monitoringConditions?.startDate = getMonitoringStartDate()
  dto.monitoringConditions?.endDate = getMonitoringEndDate()

  return dto
}
