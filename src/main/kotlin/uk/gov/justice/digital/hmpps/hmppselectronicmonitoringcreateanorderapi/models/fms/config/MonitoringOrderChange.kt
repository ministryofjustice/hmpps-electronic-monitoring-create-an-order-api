import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.OrderChangeDetail

enum class MonitoringOrderChange(
  override val message: String,
  override val orderVariationType: VariationType = VariationType.OTHER,
) : OrderChangeDetail {

  // Core order fields
  ConditionType(
    "Condition type has changed",
    VariationType.CHANGE_TO_ENFORCEABLE_CONDITION,
  ),
  OffenceAdditionalDetails(
    "Offence additional detail has changed",
    VariationType.OTHER,
  ),
  OrderStart(
    "Start date of the order has changed",
    VariationType.OTHER,
  ),
  OrderEnd(
    "End date of the order has changed",
    VariationType.OTHER,
  ),
  OrderType(
    "Order type has changed",
    VariationType.OTHER,
  ),

  // Organisations / officers
  NotifyingOrganization(
    "Notifying organisation has changed",
    VariationType.OTHER,
  ),
  NoEmail(
    "Notifying organisation email has changed",
    VariationType.OTHER,
  ),
  NoName(
    "Notifying organisation name has changed",
    VariationType.OTHER,
  ),
  PduResponsible(
    "PDU has changed",
    VariationType.OTHER,
  ),
  ResponsibleOfficerEmail(
    "Responsible officer's email has changed",
    VariationType.OTHER,
  ),
  ResponsibleOfficerName(
    "Responsible officer's name has changed",
    VariationType.OTHER,
  ),
  ResponsibleOrganization(
    "Responsible organisation has changed",
    VariationType.OTHER,
  ),
  RoEmail(
    "Responsible organisation email has changed",
    VariationType.OTHER,
  ),
  RoRegion(
    "Responsible organisation region has changed",
    VariationType.OTHER,
  ),

  // Sentence / tag
  SentenceType(
    "Device wearer's sentence changed",
    VariationType.OTHER,
  ),
  TagAtSource(
    "Tag at source has changed",
    VariationType.CHANGE_TO_DEVICE_TYPE,
  ),
  TagAtSourceDetails(
    "Name of installation location has changed",
    VariationType.CHANGE_TO_DEVICE_TYPE,
  ),
  DateAndTimeInstallationWillTakePlace(
    "Installation date and time have changed",
    VariationType.OTHER,
  ),

  // Curfew
  CurfewDescription(
    "Curfew boundary has changed",
    VariationType.CHANGE_TO_ENFORCEABLE_CONDITION,
  ),
  CurfewStart(
    "Curfew start date has changed",
    VariationType.CHANGE_TO_ENFORCEABLE_CONDITION,
  ),
  CurfewEnd(
    "Curfew end date has changed",
    VariationType.CHANGE_TO_ENFORCEABLE_CONDITION,
  ),
  ConditionalReleaseStartTime(
    "Curfew start time on day of release has changed",
    VariationType.CHANGE_TO_CURFEW_HOURS,
  ),
  ConditionalReleaseEndTime(
    "Curfew end time on day of release has changed",
    VariationType.CHANGE_TO_CURFEW_HOURS,
  ),

  // Flags / pilots
  Abstinence(
    "Alcohol abstinence has been changed",
    VariationType.CHANGE_TO_ENFORCEABLE_CONDITION,
  ),
  Issp(
    "ISSP has changed",
    VariationType.OTHER,
  ),
  Hdc(
    "HDC has changed",
    VariationType.OTHER,
  ),
  Pilot(
    "Pilot has changed",
    VariationType.OTHER,
  ),
  ReleasedUnderPrarr(
    "PRARR has changed",
    VariationType.OTHER,
  ),
  DapolMissedInError(
    "DAPOL missed in error has changed",
    VariationType.OTHER,
  ),
  InstallAtSourcePilot(
    "Install at source pilot has changed",
    VariationType.OTHER,
  ),

  // Addresses / IDs
  InstallationAddress(
    "Installation address has changed",
    VariationType.CHANGE_TO_ADDRESS,
  ),
  CourtCaseReferenceNumber(
    "Device wearer's court case reference number(s) have changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),

  // Lists / complex structures
  ExclusionZones(
    "One or more of the exclusion zones has changed",
    VariationType.CHANGE_TO_ENFORCEABLE_CONDITION,
  ),
  AcEligibleOffences(
    "Acquisitive crime offence has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  DapoOrderClauseNumbers(
    "DAPO order clause number(s) or dates have changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  Offences(
    "Offence(s) or offence date have changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
}
