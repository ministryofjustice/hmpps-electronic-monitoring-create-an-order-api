package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Up3DeviceWearer(
  var title: String,
  var firstName: String,
  var lastName: String,
  var alias: String,
  var dateOfBirth: String,
  var adultChild: String,
  var interpreterRequired: String,
  var language: String,
  var genderIdentity: String,
  var sex: String,
  var homeOfficeCaseReferenceNumber: String,
  var prisonNumber: String,
  var deliusId: String,
  var nomisId: String,
  var pncId: String,
  var noFixedAddress: String,
  var address1: String,
  var address2: String,
  var address3: String,
  var address4: String,
  var addressPostCode: String,
  var secondaryAddress1: String,
  var secondaryAddress2: String,
  var secondaryAddress3: String,
  var secondaryAddress4: String,
  var secondaryAddressPostCode: String,
  var tertiaryAddress1: String,
  var tertiaryAddress2: String,
  var tertiaryAddress3: String,
  var tertiaryAddress4: String,
  var tertiaryAddressPostCode: String,
) {

  companion object {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val londonTimeZone: ZoneId = ZoneId.of("Europe/London")
  }

  fun toDeviceWearer(versionId: UUID): DeviceWearer {
    val parsedDob = LocalDate.parse(dateOfBirth, dateFormatter).atStartOfDay(londonTimeZone)

    return DeviceWearer(
      versionId = versionId,
      firstName = firstName,
      lastName = lastName,
      alias = alias,
      dateOfBirth = parsedDob,
      adultAtTimeOfInstallation = adultChild == "adult",
      interpreterRequired = interpreterRequired == "true",
      language = language,
      gender = genderIdentity,
      sex = sex,
      pncId = pncId,
      nomisId = nomisId,
      deliusId = deliusId,
      prisonNumber = prisonNumber,
      homeOfficeReferenceNumber = homeOfficeCaseReferenceNumber, // maybe map to ho case ref number
    )
  }

  fun toAddress(versionId: UUID): Address? {
    if (noFixedAddress == "true") return null

    return Address(
      versionId = versionId,
      addressType = AddressType.PRIMARY,
      addressLine1 = address1,
      addressLine2 = address2,
      addressLine3 = address3,
      addressLine4 = address4,
      postcode = addressPostCode,
    )
  }

  fun toSecondaryAddress(versionId: UUID): Address? {
    if (secondaryAddress1.isBlank()) return null

    return Address(
      versionId = versionId,
      addressType = AddressType.SECONDARY,
      addressLine1 = secondaryAddress1,
      addressLine2 = secondaryAddress2,
      addressLine3 = secondaryAddress3,
      addressLine4 = secondaryAddress4,
      postcode = secondaryAddressPostCode,
    )
  }

  fun toTertiaryAddress(versionId: UUID): Address? {
    if (tertiaryAddress1.isBlank()) return null

    return Address(
      versionId = versionId,
      addressType = AddressType.TERTIARY,
      addressLine1 = tertiaryAddress1,
      addressLine2 = tertiaryAddress2,
      addressLine3 = tertiaryAddress3,
      addressLine4 = tertiaryAddress4,
      postcode = tertiaryAddressPostCode,
    )
  }
}
