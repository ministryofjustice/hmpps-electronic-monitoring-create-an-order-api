package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config

enum class MonitoringOrderChange(val message: String) {

  // Core order fields
  ConditionType("Condition type has changed"),
  OffenceAdditionalDetails("Offence additional detail has changed"),
  OrderStart("Start date of the order has changed"),
  OrderEnd("End date of the order has changed"),
  OrderType("Order type has changed"),

  // Organisations / officers
  NotifyingOrganization("Notifying organisation has changed"),
  NoEmail("Notifying organisation email has changed"),
  NoName("Notifying organisation name has changed"),
  PduResponsible("PDU has changed"),
  ResponsibleOfficerEmail("Responsible officer's email has changed"),
  ResponsibleOfficerName("Responsible officer's name has changed"),
  ResponsibleOrganization("Responsible organisation has changed"),
  RoEmail("Responsible organisation email has changed"),
  RoRegion("Responsible organisation region has changed"),

  // Sentence / tag
  SentenceType("Device wearer's sentence changed"),
  TagAtSource("Tag at source has changed"),
  TagAtSourceDetails("Name of installation location has changed"),
  DateAndTimeInstallationWillTakePlace(
    "Installation date and time have changed",
  ),

  // Curfew
  CurfewDescription("Curfew boundary has changed"),
  CurfewStart("Curfew start date has changed"),
  CurfewEnd("Curfew end date has changed"),
  ConditionalReleaseStartTime("Curfew start time on day of release has changed"),
  ConditionalReleaseEndTime("Curfew end time on day of release has changed"),

  // Flags / pilots
  Abstinence("Alcohol abstinence has been changed"),
  Issp("ISSP has changed"),
  Hdc("HDC has changed"),
  Pilot("Pilot has changed"),
  ReleasedUnderPrarr("PRARR has changed"),
  DapolMissedInError("DAPOL missed in error has changed"),
  InstallAtSourcePilot("Install at source pilot has changed"),

  // Addresses / IDs
  InstallationAddress("Installation address has changed"),
  CourtCaseReferenceNumber("Device wearer's court case reference number(s) have changed"),

  // Lists / complex structures
  ExclusionZones("One or more of the exclusion zones has changed"),
  AcEligibleOffences("Acquisitive crime offence has changed"),
  DapoOrderClauseNumbers("DAPO order clause number(s) or dates have changed"),
  Offences("Offence(s) or offence date have changed"),
}
