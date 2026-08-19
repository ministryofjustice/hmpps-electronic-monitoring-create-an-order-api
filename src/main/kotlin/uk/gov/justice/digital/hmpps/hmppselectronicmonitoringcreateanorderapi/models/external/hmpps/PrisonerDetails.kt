package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDates.londonTimeZone
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class PrisonerDetails(
  val firstName: String?,
  val middleNames: String?,
  val lastName: String?,
  val dateOfBirth: String?,
  val sex: CodeDescription?,
  val identifiers: Identifiers?,
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
    dateOfBirth = parseDateOrNull(dateOfBirth ?: ""),
    sex = sex?.toSex(),
  )

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
