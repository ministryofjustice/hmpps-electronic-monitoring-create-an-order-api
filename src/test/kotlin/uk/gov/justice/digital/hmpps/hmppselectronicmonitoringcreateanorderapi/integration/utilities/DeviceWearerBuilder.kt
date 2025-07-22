package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class DeviceWearerBuilder(var versionId: UUID, var noFixedAbode: Boolean) {
  var firstName: String = "John"
  var lastName: String = "Smith"
  var alias: String? = "Johnny"
  var dateOfBirth: ZonedDateTime = ZonedDateTime.of(1990, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault())
  var adultAtTimeOfInstallation: Boolean = true
  var sex: String = "MALE"
  var gender: String = "MALE"
  var disabilities: String = "VISION,LEARNING_UNDERSTANDING_CONCENTRATING"
  var interpreterRequired: Boolean = true
  var language: String = "British Sign"
  var pncId: String? = "pncId"
  var deliusId: String? = "deliusId"
  var nomisId: String? = "nomisId"
  var prisonNumber: String? = "prisonNumber"
  var homeOfficeReferenceNumber: String? = "homeOfficeReferenceNumber"

  fun build(): DeviceWearer = DeviceWearer(
    versionId = versionId,
    firstName = firstName,
    lastName = lastName,
    alias = alias,
    dateOfBirth = dateOfBirth,
    adultAtTimeOfInstallation = adultAtTimeOfInstallation,
    sex = sex,
    gender = gender,
    disabilities = disabilities,
    interpreterRequired = interpreterRequired,
    language = language,
    pncId = pncId,
    deliusId = deliusId,
    nomisId = nomisId,
    prisonNumber = prisonNumber,
    homeOfficeReferenceNumber = homeOfficeReferenceNumber,
    noFixedAbode = noFixedAbode,
  )
}
