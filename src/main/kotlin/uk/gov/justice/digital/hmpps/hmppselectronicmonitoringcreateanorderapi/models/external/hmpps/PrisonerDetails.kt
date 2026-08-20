package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
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
  )

  fun toPrimaryAddress(versionId: UUID): CemoAddress? {
    val primaryAddress = addresses?.firstOrNull { it.isPrimaryAddress() } ?: return null

    return primaryAddress.toCemoAddress(versionId)
  }

  fun toSecondaryAddress(versionId: UUID): CemoAddress? {
    val secondaryAddress = addresses?.firstOrNull { it.isSecondaryAddress() } ?: return null

    return secondaryAddress.toCemoAddress(versionId)
  }

  fun parsedDateOfBirth(): ZonedDateTime? = parseDateOrNull(dateOfBirth ?: "")

  fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(londonTimeZone)

  fun parseDateOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDate(date) else null
}

data class CodeDescription(val code: String?, val description: String?) {
  fun toSex(): String? = when (code) {
    "M" -> "MALE"
    "F" -> "FEMALE"
    "NS" -> "PREFER_NOT_TO_SAY"
    else -> "UNKNOWN"
  }
}

data class Identifiers(
  val crns: List<String> = emptyList(),
  val prisonNumbers: List<String> = emptyList(),
  val pncs: List<String> = emptyList(),
)

data class Alias(val firstName: String?, val lastName: String?, val middleNames: String?) {
  fun getAlias(): String = "$firstName $middleNames $lastName"
}

data class Address(
  val noFixedAbode: Boolean?,
  val postcode: String?,
  val status: CodeDescription?,
  val buildingNumber: String?,
  val buildingName: String?,
  val subBuildingName: String?,
  val postTown: String?,
  val county: String?,
  val thoroughfareName: String?,
) {
  fun isPrimaryAddress(): Boolean = this.status?.code == "M"

  fun isSecondaryAddress(): Boolean = this.status?.code == "S"

  fun toCemoAddress(versionId: UUID): CemoAddress = CemoAddress(
    versionId = versionId,
    addressLine1 = addressLineOne(),
    addressLine2 = "",
    addressLine3 = postTown?.toTitleCase() ?: "",
    addressLine4 = county?.toTitleCase() ?: "",
    postcode = postcode ?: "",
    addressType = addressType(),
  )

  private fun addressType(): AddressType = if (isPrimaryAddress()) AddressType.PRIMARY else AddressType.SECONDARY

  private fun addressLineOne(): String {
    val buildingId = buildingNumber.takeIf { !it.isNullOrEmpty() } ?: buildingName.takeIf { !it.isNullOrEmpty() } ?: ""

    if (thoroughfareName.isNullOrEmpty()) {
      return buildingId
    }

    return "$buildingId $thoroughfareName".toTitleCase()
  }

  private fun String.toTitleCase(): String = lowercase().split(" ").joinToString(" ") {
    it.replaceFirstChar { char -> char.uppercaseChar() }
  }
}
