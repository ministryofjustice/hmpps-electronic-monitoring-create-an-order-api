package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import java.time.ZonedDateTime

data class UpdateDeviceWearerDto(
  @field:Size(min = 1, message = "First name is required")
  val firstName: String? = null,

  @field:Size(min = 1, message = "Last name is required")
  val lastName: String? = null,

  val alias: String? = null,

  @field:NotNull(
    message = "You must indicate whether the device wearer will be an adult at installation",
  )
  var adultAtTimeOfInstallation: Boolean? = null,

  @field:Size(min = 1, message = "Sex is required")
  var sex: String? = null,

  @field:Size(min = 1, message = "Gender is required")
  val gender: String? = null,

  @field:NotNull(message = "Date of birth is required")
  @field:Past(message = "Date of birth must be in the past")
  val dateOfBirth: ZonedDateTime? = null,

  val disabilities: String? = null,

  val language: String? = null,

  @field:NotNull(
    message = "You must indicate whether the device wearer will require an interpreter on the day of installation",
  )
  val interpreterRequired: Boolean? = null,
) {

  @AssertTrue(message = "Device wearer's main language is required")
  fun isLanguage(): Boolean {
    if (this.interpreterRequired != null && this.interpreterRequired) {
      return this.language != null && this.language != ""
    }
    return true
  }
}
