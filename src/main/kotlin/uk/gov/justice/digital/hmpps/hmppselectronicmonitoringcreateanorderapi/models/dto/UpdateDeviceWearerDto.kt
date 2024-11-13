package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import java.time.ZonedDateTime

data class UpdateDeviceWearerDto(
  val nomisId: String? = null,

  val pncId: String? = null,

  val deliusId: String? = null,

  val prisonNumber: String? = null,

  var homeOfficeReferenceNumber: String? = null,

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

  val selfIdentifyGender: String? = null,

  @field:NotNull(message = "Date of birth is required")
  @field:Past(message = "Date of birth must be in the past")
  val dateOfBirth: ZonedDateTime? = null,

  val disabilities: String? = null,

  val otherDisabilities: String? = null,

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

  @AssertTrue(message = "Enter the self-identified gender of the device wearer or select a different option")
  fun isSelfIdentifyGender(): Boolean {
    return !(this.gender == "self-identify" && this.selfIdentifyGender.isNullOrBlank())
  }

  @AssertTrue(message = "Enter the device wearer's other disabilities or deselect 'other'")
  fun isOtherDisabilities(): Boolean {
    return !(this.disabilities?.contains("Other") == true && this.otherDisabilities.isNullOrBlank())
  }
}
