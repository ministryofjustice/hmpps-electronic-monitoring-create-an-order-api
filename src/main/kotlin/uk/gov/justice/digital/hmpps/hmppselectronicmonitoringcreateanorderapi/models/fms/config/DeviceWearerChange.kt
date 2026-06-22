package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType

enum class DeviceWearerChange(
  override val message: String,
  override val orderVariationType: VariationType = VariationType.OTHER,
) : OrderChangeDetail {
  NameChange(
    "Device wearer's name has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  Alias(
    "Device wearer's preferred name has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  DateOfBirth(
    "Device wearer's date of birth has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  AdultToChild(
    "Order has changed from an adult to youth",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  ChildToAdult(
    "Order has changed from a youth to adult",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  Sex(
    "Device wearer's sex has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  GenderIdentity(
    "Device wearer's gender has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  Disability(
    "Device wearer's disability or health conditions have changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  PrimaryAddressChange(
    "Device wearer's main address has changed",
    VariationType.CHANGE_TO_ADDRESS,
  ),
  SecondaryAddressChange(
    "Device wearer's secondary address has changed",
    VariationType.CHANGE_TO_ADDRESS,
  ),
  TertiaryAddressChange(
    "Device wearer's tertiary address has changed",
    VariationType.CHANGE_TO_ADDRESS,
  ),
  NoFixedAddress(
    "Device wearer now doesn't have a fixed address",
    VariationType.CHANGE_TO_ADDRESS,
  ),
  HasFixedAddress(
    "Device wearer now has a fixed address",
    VariationType.CHANGE_TO_ADDRESS,
  ),
  PhoneNumber(
    "Device wearer's phone number has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  Mappa(
    "Device wearer's MAPPA level has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  MappaCategory(
    "Device wearer's MAPPA category has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  MappaCaseType(
    "Device wearer's MAPPA case type has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  RiskCategory(
    "Device wearer's risk categories have changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  PersonalIdChanged(
    "Device wearer's personal ID number(s) have changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  InterpreterRequired(
    "Device wearer's interpreter needs have changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  ResponsibleAdultChanged(
    "Responsible adult's details have changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
  ParentPhoneNumber(
    "Responsible adult's phone number has changed",
    VariationType.CHANGE_TO_PERSONAL_DETAILS,
  ),
}
