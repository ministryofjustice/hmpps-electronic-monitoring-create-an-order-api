package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.AcEligibleOffence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DapoClause
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.OffenceData
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Schedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Zone

data class MonitoringOrderFieldCase(
  val name: String,
  val mutate: (MonitoringOrder) -> Unit,
  val expectedMessage: String? = null,
) {
  override fun toString(): String = name
}

class MonitoringOrderFieldChangeArgumentsProvider : ArgumentsProvider {

  override fun provideArguments(context: ExtensionContext) = listOf(

    // ✅ Core order fields
    MonitoringOrderFieldCase(
      "conditionType",
      { it.conditionType = "Changed" },
      "Condition type has changed",
    ),

    MonitoringOrderFieldCase(
      "offenceAdditionalDetails",
      { it.offenceAdditionalDetails = "New details" },
      "Offence additional detail has changed",
    ),

    MonitoringOrderFieldCase(
      "orderStart",
      { it.orderStart = "2025-02-01" },
      "Start date of the order has changed",
    ),

    MonitoringOrderFieldCase(
      "orderEnd",
      { it.orderEnd = "2025-03-01" },
      "End date of the order has changed",
    ),

    MonitoringOrderFieldCase(
      "orderType",
      { it.orderType = "NewType" },
      "Order type has changed",
    ),

    // ✅ Organisation / notifying
    MonitoringOrderFieldCase(
      "notifyingOrganization",
      { it.notifyingOrganization = "New Org" },
      "Notifying organisation has changed",
    ),

    MonitoringOrderFieldCase(
      "noEmail",
      { it.noEmail = "new@email.com" },
      "Notifying organisation email has changed",
    ),

    MonitoringOrderFieldCase(
      "noName",
      { it.noName = "New Name" },
      "Notifying organisation name has changed",
    ),

    MonitoringOrderFieldCase(
      "pduResponsible",
      { it.pduResponsible = "New PDU" },
      "PDU has changed",
    ),

    // ✅ Responsible officer
    MonitoringOrderFieldCase(
      "responsibleOfficerEmail",
      { it.responsibleOfficerEmail = "new@officer.com" },
      "Responsible officer's email has changed",
    ),

    MonitoringOrderFieldCase(
      "responsibleOfficerName",
      { it.responsibleOfficerName = "Officer Name" },
      "Responsible officer's name has changed",
    ),

    MonitoringOrderFieldCase(
      "responsibleOrganization",
      { it.responsibleOrganization = "Police" },
      "Responsible organisation has changed",
    ),

    MonitoringOrderFieldCase(
      "roEmail",
      { it.roEmail = "ro@email.com" },
      "Responsible organisation email has changed",
    ),

    MonitoringOrderFieldCase(
      "roRegion",
      { it.roRegion = "New Region" },
      "Responsible organisation region has changed",
    ),

    // ✅ Sentence / tagging
    MonitoringOrderFieldCase(
      "sentenceType",
      { it.sentenceType = "New Sentence" },
      "Device wearer's sentence changed",
    ),

    MonitoringOrderFieldCase(
      "tagAtSource",
      { it.tagAtSource = "Yes" },
      "Tag at source has changed",
    ),

    MonitoringOrderFieldCase(
      "tagAtSourceDetails",
      { it.tagAtSourceDetails = "New Site" },
      "Name of installation location has changed",
    ),

    MonitoringOrderFieldCase(
      "dateAndTimeInstallationWillTakePlace",
      { it.dateAndTimeInstallationWillTakePlace = "2025-02-01T10:00" },
      "Installation date and time have changed",
    ),

    // ✅ Curfew
    MonitoringOrderFieldCase(
      "curfewDescription",
      { it.curfewDescription = "Updated boundary" },
      "Curfew boundary has changed",
    ),

    MonitoringOrderFieldCase(
      "curfewStart",
      { it.curfewStart = "18:00" },
      "Curfew start time has changed",
    ),

    MonitoringOrderFieldCase(
      "curfewEnd",
      { it.curfewEnd = "07:00" },
      "Curfew end date has changed",
    ),

    // ✅ Flags / pilot
    MonitoringOrderFieldCase(
      "trailMonitoring",
      { it.trailMonitoring = "Yes" },
      "Trail monitoring has been added or removed",
    ),

    MonitoringOrderFieldCase(
      "abstinence",
      { it.abstinence = "Yes" },
      "Alcohol abstinence has been changed",
    ),

    MonitoringOrderFieldCase(
      "issp",
      { it.issp = "Yes" },
      "ISSP has changed",
    ),

    MonitoringOrderFieldCase(
      "hdc",
      { it.hdc = "Yes" },
      "HDC has changed",
    ),

    MonitoringOrderFieldCase(
      "pilot",
      { it.pilot = "Pilot1" },
      "Pilot has changed",
    ),

    MonitoringOrderFieldCase(
      "releasedUnderPrarr",
      { it.releasedUnderPrarr = "Yes" },
      "PRARR has changed",
    ),

    MonitoringOrderFieldCase(
      "dapolMissedInError",
      { it.dapolMissedInError = "Yes" },
      "DAPOL missed in error has changed",
    ),

    MonitoringOrderFieldCase(
      "installAtSourcePilot",
      { it.installAtSourcePilot = "Yes" },
      "Install at source pilot has changed",
    ),

    // ✅ Installation address (grouped)
    MonitoringOrderFieldCase(
      "installationAddress",
      { it.installationAddress1 = "New Address Line 1" },
      "Installation address has changed",
    ),

    MonitoringOrderFieldCase(
      "installationAddress",
      { it.installationAddress2 = "New Address Line 2" },
      "Installation address has changed",
    ),

    MonitoringOrderFieldCase(
      "installationAddress",
      { it.installationAddress3 = "New Address Line 3" },
      "Installation address has changed",
    ),

    MonitoringOrderFieldCase(
      "installationAddress",
      { it.installationAddress4 = "New Address Line 4" },
      "Installation address has changed",
    ),

    MonitoringOrderFieldCase(
      "installationAddress",
      { it.installationAddressPostcode = "New Address Post code" },
      "Installation address has changed",
    ),

    // ✅ IDs
    MonitoringOrderFieldCase(
      "crownCourtCaseReferenceNumber",
      { it.crownCourtCaseReferenceNumber = "NEW123" },
      "Device wearer's personal ID number(s) have changed",
    ),

    // ✅ LIST FIELDS

    MonitoringOrderFieldCase(
      "exclusionZones",
      {
        it.exclusionZones = mutableListOf(
          Zone("Zone1", "1h", "10:00", "11:00"),
        )
      },
      "One or more of the exclusion zones has changed",
    ),

    MonitoringOrderFieldCase(
      "acEligibleOffences",
      {
        it.acEligibleOffences = mutableListOf(
          AcEligibleOffence("Theft", "2025-01-01"),
        )
      },
      "Acquisitive crime offence has changed",
    ),

    MonitoringOrderFieldCase(
      "dapoOrderClauseNumbers",
      {
        it.dapoOrderClauseNumbers = mutableListOf(
          DapoClause("Clause1", "2025-01-01"),
        )
      },
      "DAPO order clause number(s) or dates have changed",
    ),

    MonitoringOrderFieldCase(
      "offences",
      {
        it.offences = mutableListOf(
          OffenceData("Burglary", "2025-01-01"),
        )
      },
      "Offence(s) or offence date have changed",
    ),

  ).map { Arguments.of(it) }.stream()
}

class MonitoringOrderNegativeArgumentsProvider : ArgumentsProvider {

  override fun provideArguments(context: ExtensionContext) = listOf(

    MonitoringOrderFieldCase("caseId", { it.caseId = "NEW_CASE" }),
    MonitoringOrderFieldCase("alldayLockdown", { it.alldayLockdown = "Y" }),
    MonitoringOrderFieldCase("atvAllowance", { it.atvAllowance = "10" }),
    MonitoringOrderFieldCase("court", { it.court = "New Court" }),
    MonitoringOrderFieldCase("courtOrderEmail", { it.courtOrderEmail = "court@test.com" }),
    MonitoringOrderFieldCase("deviceType", { it.deviceType = "GPS" }),
    MonitoringOrderFieldCase("deviceWearer", { it.deviceWearer = "New Wearer" }),

    MonitoringOrderFieldCase("exclusionAllday", { it.exclusionAllday = "true" }),
    MonitoringOrderFieldCase("interimCourtDate", { it.interimCourtDate = "2026-01-01" }),
    MonitoringOrderFieldCase("issuingOrganisation", { it.issuingOrganisation = "Court Org" }),
    MonitoringOrderFieldCase("mediaInterest", { it.mediaInterest = "Yes" }),
    MonitoringOrderFieldCase("newOrderReceived", { it.newOrderReceived = "Yes" }),

    MonitoringOrderFieldCase("notifyingOfficerEmail", {
      it.notifyingOfficerEmail = "officer@test.com"
    }),
    MonitoringOrderFieldCase("notifyingOfficerName", {
      it.notifyingOfficerName = "Officer X"
    }),

    MonitoringOrderFieldCase("noPostCode", { it.noPostCode = "NP1 1AA" }),
    MonitoringOrderFieldCase("noAddress1", { it.noAddress1 = "Line1" }),
    MonitoringOrderFieldCase("noAddress2", { it.noAddress2 = "Line2" }),
    MonitoringOrderFieldCase("noAddress3", { it.noAddress3 = "Line3" }),
    MonitoringOrderFieldCase("noAddress4", { it.noAddress4 = "Line4" }),
    MonitoringOrderFieldCase("noPhoneNumber", { it.noPhoneNumber = "07000000000" }),

    MonitoringOrderFieldCase("offence", { it.offence = "Burglary" }),
    MonitoringOrderFieldCase("offenceDate", { it.offenceDate = "2025-01-01" }),

    MonitoringOrderFieldCase("orderId", { it.orderId = "ORD1" }),
    MonitoringOrderFieldCase("orderRequestType", { it.orderRequestType = "NEW" }),
    MonitoringOrderFieldCase("orderTypeDescription", { it.orderTypeDescription = "Desc" }),
    MonitoringOrderFieldCase("orderTypeDetail", { it.orderTypeDetail = "Detail" }),
    MonitoringOrderFieldCase("orderVariationDate", { it.orderVariationDate = "2025-01-01" }),
    MonitoringOrderFieldCase("orderVariationDetails", { it.orderVariationDetails = "Changed" }),
    MonitoringOrderFieldCase("orderVariationReqReceivedDate", { it.orderVariationReqReceivedDate = "2025-01-01" }),
    MonitoringOrderFieldCase("orderVariationType", { it.orderVariationType = "TYPE" }),

    MonitoringOrderFieldCase("pduResponsibleEmail", { it.pduResponsibleEmail = "pdu@test.com" }),
    MonitoringOrderFieldCase("plannedOrderEndDate", { it.plannedOrderEndDate = "2025-12-31" }),
    MonitoringOrderFieldCase("responsibleOfficerDetailsReceived", {
      it.responsibleOfficerDetailsReceived = "Yes"
    }),
    MonitoringOrderFieldCase("responsibleOfficerPhone", {
      it.responsibleOfficerPhone = "07000000000"
    }),

    MonitoringOrderFieldCase("roPostCode", { it.roPostCode = "RO1" }),
    MonitoringOrderFieldCase("roAddress1", { it.roAddress1 = "Addr1" }),
    MonitoringOrderFieldCase("roAddress2", { it.roAddress2 = "Addr2" }),
    MonitoringOrderFieldCase("roAddress3", { it.roAddress3 = "Addr3" }),
    MonitoringOrderFieldCase("roAddress4", { it.roAddress4 = "Addr4" }),
    MonitoringOrderFieldCase("roPhone", { it.roPhone = "07000000000" }),

    MonitoringOrderFieldCase("sentenceDate", { it.sentenceDate = "2025-01-01" }),
    MonitoringOrderFieldCase("sentenceExpiry", { it.sentenceExpiry = "2026-01-01" }),

    MonitoringOrderFieldCase("technicalBail", { it.technicalBail = "Yes" }),
    MonitoringOrderFieldCase("trialDate", { it.trialDate = "2026-01-01" }),
    MonitoringOrderFieldCase("trialOutcome", { it.trialOutcome = "Success" }),
    MonitoringOrderFieldCase("conditionalReleaseDate", { it.conditionalReleaseDate = "2025-01-01" }),
    MonitoringOrderFieldCase("conditionalReleaseStartTime", {
      it.conditionalReleaseStartTime = "09:00"
    }),
    MonitoringOrderFieldCase("conditionalReleaseEndTime", {
      it.conditionalReleaseEndTime = "17:00"
    }),

    MonitoringOrderFieldCase("reasonForOrderEndingEarly", {
      it.reasonForOrderEndingEarly = "Completed"
    }),

    MonitoringOrderFieldCase("businessUnit", { it.businessUnit = "Unit1" }),
    MonitoringOrderFieldCase("serviceEndDate", { it.serviceEndDate = "2026-01-01" }),

    MonitoringOrderFieldCase("schedule", { it.schedule = "Updated" }),
    MonitoringOrderFieldCase("checkinSchedule", {
      it.checkinSchedule = mutableListOf(
        Schedule("Mo", "10:00", "11:00"),
      )
    }),

    MonitoringOrderFieldCase("revocationDate", { it.revocationDate = "2025-05-01" }),
    MonitoringOrderFieldCase("revocationType", { it.revocationType = "TypeA" }),

    MonitoringOrderFieldCase("magistrateCourtCaseReferenceNumber", {
      it.magistrateCourtCaseReferenceNumber = "MAG123"
    }),

    MonitoringOrderFieldCase("orderStatus", { it.orderStatus = "Active" }),
    MonitoringOrderFieldCase("subcategory", { it.subcategory = "Sub1" }),

  ).map { Arguments.of(it) }
    .stream()
}
