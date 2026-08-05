package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import java.time.LocalDateTime
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
) {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  fun toDeviceWearer(versionId: UUID): DeviceWearer {
    val parsedDob = LocalDateTime.parse(dateOfBirth, formatter).atZone(ZoneId.of("UTC"))

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
    )
  }
}
