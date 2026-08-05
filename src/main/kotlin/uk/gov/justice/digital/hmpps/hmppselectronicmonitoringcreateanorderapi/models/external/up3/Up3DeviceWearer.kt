package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
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
      complianceAndEnforcementPersonReference = homeOfficeCaseReferenceNumber, // maybe map to ho case ref number
    )
  }
}
