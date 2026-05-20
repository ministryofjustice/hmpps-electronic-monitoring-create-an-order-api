package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config

object MonitoringOrderChangedMessages {

  val messages: Map<String, String> = mapOf(

    // Core order fields
    "conditionType" to "Condition type has changed",
    "offenceAdditionalDetails" to "Offence additional detail has changed",
    "orderStart" to "Start date of the order has changed",
    "orderEnd" to "End date of the order has changed",
    "orderType" to "Order type has changed",

    // Organisations / officers
    "notifyingOrganization" to "Notifying organisation has changed",
    "noEmail" to "Notifying organisation email has changed",
    "noName" to "Notifying organisation name has changed",
    "pduResponsible" to "PDU has changed",
    "responsibleOfficerEmail" to "Responsible officer's email has changed",
    "responsibleOfficerName" to "Responsible officer's name has changed",
    "responsibleOrganization" to "Responsible organisation has changed",
    "roEmail" to "Responsible organisation email has changed",
    "roRegion" to "Responsible organisation region has changed",

    // Sentence / tag
    "sentenceType" to "Device wearer's sentence changed",
    "tagAtSource" to "Tag at source has changed",
    "tagAtSourceDetails" to "Name of installation location has changed",
    "dateAndTimeInstallationWillTakePlace" to "Installation date and time have changed",

    // Curfew
    "curfewDescription" to "Curfew boundary has changed",
    "curfewStart" to "Curfew start date has changed",
    "curfewEnd" to "Curfew end date has changed",

    // Flags / pilots
    "abstinence" to "Alcohol abstinence has been changed",
    "issp" to "ISSP has changed",
    "hdc" to "HDC has changed",
    "pilot" to "Pilot has changed",
    "releasedUnderPrarr" to "PRARR has changed",
    "dapolMissedInError" to "DAPOL missed in error has changed",
    "installAtSourcePilot" to "Install at source pilot has changed",

    // Addresses / IDs
    "installationAddress" to "Installation address has changed",
    "courtCaseReferenceNumber" to "Device wearer's court case reference number(s) have changed",

    // Lists / complex structures
    "exclusionZones" to "One or more of the exclusion zones has changed",
    "acEligibleOffences" to "Acquisitive crime offence has changed",
    "dapoOrderClauseNumbers" to "DAPO order clause number(s) or dates have changed",
    "offences" to "Offence(s) or offence date have changed",
  )
}
