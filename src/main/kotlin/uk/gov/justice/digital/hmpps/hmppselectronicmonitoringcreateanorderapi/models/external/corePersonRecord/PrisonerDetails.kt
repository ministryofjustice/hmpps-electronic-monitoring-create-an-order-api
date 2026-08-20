package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDates.londonTimeZone
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address as CemoAddress

data class PrisonerDetails(
  val firstName: String?,
  val middleNames: String?,
  val lastName: String?,
  val dateOfBirth: String?,
  val sex: CodeDescription?,
  val identifiers: Identifiers?,
  val aliases: List<Alias>?,
  val addresses: List<Address>?,
  val religion: CodeDescription?,
  val ethnicity: CodeDescription?,
  val nationalities: List<CodeDescription>,
) {

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  fun toDeviceWearer(versionId: UUID): DeviceWearer = DeviceWearer(
    versionId = versionId,
    firstName = firstName,
    middleName = middleNames,
    lastName = lastName,
    prisonNumber = identifiers?.prisonNumbers?.first(),
    courtCaseReferenceNumber = identifiers?.crns?.first(),
    pncId = identifiers?.pncs?.first(),
    dateOfBirth = parsedDateOfBirth(),
    sex = sex?.toSex(),
    alias = aliases?.firstOrNull()?.getAlias(),
    noFixedAbode = addresses?.any { it.noFixedAbode ?: false },
    religion = religion?.description,
    nationality = nationalities.firstOrNull()?.description,
    ethnicity = ethnicity?.description,
  )

  fun toPrimaryAddress(versionId: UUID): CemoAddress? {
    val primaryAddress = addresses?.firstOrNull { it.isPrimaryAddress() } ?: return null

    return primaryAddress.toCemoAddress(versionId)
  }

  fun toSecondaryAddress(versionId: UUID): CemoAddress? {
    val secondaryAddress = addresses?.firstOrNull { it.isSecondaryAddress() } ?: return null

    return secondaryAddress.toCemoAddress(versionId)
  }

  fun toContactDetails(versionId: UUID): ContactDetails? {
    val contacts = addresses?.flatMap { it.contacts } ?: return null
    val matchingContact = contacts.firstOrNull { it.isMobile() } ?: contacts.firstOrNull { it.isHome() } ?: return null

    return ContactDetails(versionId = versionId, contactNumber = matchingContact.value, phoneNumberAvailable = true)
  }

  fun parsedDateOfBirth(): ZonedDateTime? = parseDateOrNull(dateOfBirth ?: "")

  fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(londonTimeZone)

  fun parseDateOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDate(date) else null
}
