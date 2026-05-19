package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config

object DeviceWearerChangedMessages {

  val messages: Map<String, String> = mapOf(
    "nameChange" to "Device wearer's name has changed",
    "alias" to "Device wearer's preferred name has changed",
    "dateOfBirth" to "Device wearer's date of birth has changed",
    "adultToChild" to "Order has changed from a adult to youth",
    "childToAdult" to "Order has changed from a youth to adult",
    "sex" to "Device wearer's sex has changed",
    "genderIdentity" to "Device wearer's gender has changed",
    "disability" to "Device wearer's disability or health conditions have changed",
    "primaryAddressChange" to "Device wearer's main address has changed",
    "secondaryAddressChange" to "Device wearer's secondary address has changed",
    "tertiaryAddressChange" to "Device wearer's tertiary address has changed",
    "noFixedAddress" to "Device wearer now doesn't have a fixed address",
    "hasFixedAddress" to "Device wearer now has a fixed address",
    "phoneNumber" to "Device wearer's phone number has changed",
    "mappa" to "Device wearer's MAPPA level has changed",
    "mappaCategory" to "Device wearer's MAPPA category has changed",
    "mappaCaseType" to "Device wearer's MAPPA case type has changed",
    "riskCategory" to "Device wearer's risk categories have changed",
    "personalIdChanged" to "Device wearer's personal ID number(s) have changed",
    "interpreterRequired" to "Device wearer's interpreter needs have changed",
    "responsibleAdultChanged" to "Responsible adult's details have changed",
    "parentPhoneNumber" to "Responsible adult's phone number has changed",
  )
}
