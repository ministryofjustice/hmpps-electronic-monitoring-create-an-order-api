package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.PrisonerRecord
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Alias
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.CodeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Contact
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.PrisonerDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDates.londonTimeZone
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address as CemoAddress

@Component
class CorePersonRecordApiClient : PrisonerDetailsApi {

  companion object {
    val PLACEHOLDER_VERSION_ID: UUID = UUID(0, 0)
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  }

  override fun getPrisonerDetails(prisonNumber: String): PrisonerRecord {
    val details = fetchPrisonerDetails(prisonNumber)

    return mapToPrisonerRecord(details)
  }

  private fun fetchPrisonerDetails(prisonNumber: String): PrisonerDetails {
    TODO("Not yet implemented")
  }

  internal fun mapToPrisonerRecord(details: PrisonerDetails): PrisonerRecord = PrisonerRecord(
    deviceWearer = toDeviceWearer(details),
    contactDetails = toContactDetails(details),
    addresses = listOfNotNull(toPrimaryAddress(details), toSecondaryAddress(details)),
  )

  private fun toDeviceWearer(details: PrisonerDetails): DeviceWearer = DeviceWearer(
    versionId = PLACEHOLDER_VERSION_ID,
    firstName = details.firstName,
    middleName = details.middleNames,
    lastName = details.lastName,
    prisonNumber = details.identifiers?.prisonNumbers?.first(),
    courtCaseReferenceNumber = details.identifiers?.crns?.first(),
    pncId = details.identifiers?.pncs?.first(),
    dateOfBirth = parsedDateOfBirth(details.dateOfBirth),
    sex = toSex(details.sex),
    alias = details.aliases?.firstOrNull()?.let(::toAlias),
    noFixedAbode = details.addresses?.any { it.noFixedAbode ?: false },
    religion = details.religion?.description,
    nationality = details.nationalities.firstOrNull()?.description,
    ethnicity = details.ethnicity?.description,
  )

  private fun toPrimaryAddress(details: PrisonerDetails): CemoAddress? {
    val primaryAddress = details.addresses?.firstOrNull { isPrimaryAddress(it) } ?: return null

    return toCemoAddress(primaryAddress)
  }

  private fun toSecondaryAddress(details: PrisonerDetails): CemoAddress? {
    val secondaryAddress = details.addresses?.firstOrNull { isSecondaryAddress(it) } ?: return null

    return toCemoAddress(secondaryAddress)
  }

  private fun toContactDetails(details: PrisonerDetails): ContactDetails? {
    val contacts = details.addresses?.flatMap { it.contacts } ?: return null
    val matchingContact = contacts.firstOrNull { isMobile(it) } ?: contacts.firstOrNull { isHome(it) } ?: return null

    return ContactDetails(
      versionId = PLACEHOLDER_VERSION_ID,
      contactNumber = matchingContact.value,
      phoneNumberAvailable = true,
    )
  }

  private fun toCemoAddress(address: Address): CemoAddress = CemoAddress(
    versionId = PLACEHOLDER_VERSION_ID,
    addressLine1 = addressLineOne(address),
    addressLine2 = "",
    addressLine3 = address.postTown?.toTitleCase() ?: "",
    addressLine4 = address.county?.toTitleCase() ?: "",
    postcode = address.postcode ?: "",
    addressType = if (isPrimaryAddress(address)) AddressType.PRIMARY else AddressType.SECONDARY,
  )

  private fun isPrimaryAddress(address: Address): Boolean = address.status?.code == "M"

  private fun isSecondaryAddress(address: Address): Boolean = address.status?.code == "S"

  private fun isMobile(contact: Contact): Boolean = contact.type?.code == "MOBILE"

  private fun isHome(contact: Contact): Boolean = contact.type?.code == "HOME"

  private fun toSex(sex: CodeDescription?): String? = when (sex?.code) {
    "M" -> "MALE"
    "F" -> "FEMALE"
    "NS" -> "PREFER_NOT_TO_SAY"
    else -> "UNKNOWN"
  }

  private fun toAlias(alias: Alias): String = "${alias.firstName} ${alias.middleNames} ${alias.lastName}"

  private fun addressLineOne(address: Address): String {
    val buildingId = address.buildingNumber.takeIf { !it.isNullOrEmpty() }
      ?: address.buildingName.takeIf { !it.isNullOrEmpty() } ?: ""

    if (address.thoroughfareName.isNullOrEmpty()) {
      return buildingId
    }

    return "$buildingId ${address.thoroughfareName}".toTitleCase()
  }

  private fun parsedDateOfBirth(dateOfBirth: String?): ZonedDateTime? = parseDateOrNull(dateOfBirth ?: "")

  private fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(londonTimeZone)

  private fun parseDateOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDate(date) else null

  private fun String.toTitleCase(): String = lowercase().split(" ").joinToString(" ") {
    it.replaceFirstChar { char -> char.uppercaseChar() }
  }
}
