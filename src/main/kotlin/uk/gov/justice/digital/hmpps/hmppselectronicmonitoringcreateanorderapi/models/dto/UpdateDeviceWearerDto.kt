package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Gender
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Sex
import java.time.ZonedDateTime

data class UpdateDeviceWearerDto(
  @field:Size(min = 1, message = ValidationErrors.DeviceWearer.FIRST_NAME_REQUIRED)
  val firstName: String? = null,

  @field:Size(min = 1, message = ValidationErrors.DeviceWearer.LAST_NAME_REQUIRED)
  val lastName: String? = null,

  val alias: String? = null,

  @field:NotNull(
    message = ValidationErrors.DeviceWearer.IS_ADULT_REQUIRED,
  )
  var adultAtTimeOfInstallation: Boolean? = null,

  var sex: String? = null,

  val gender: String? = null,

  @field:NotNull(message = ValidationErrors.DeviceWearer.DOB_REQUIRED)
  @field:Past(message = ValidationErrors.DeviceWearer.DOB_MUST_BE_IN_PAST)
  val dateOfBirth: ZonedDateTime? = null,

  val disabilities: String? = null,

  val otherDisability: String? = null,

  val language: String? = null,

  @field:NotNull(
    message = ValidationErrors.DeviceWearer.INTERPRETER_REQUIRED,
  )
  val interpreterRequired: Boolean? = null,
) {

  @AssertTrue(message = ValidationErrors.DeviceWearer.LANGUAGE_REQUIRED)
  fun isLanguage(): Boolean {
    if (this.interpreterRequired != null && this.interpreterRequired) {
      return this.language != null && this.language != ""
    }
    return true
  }

  @AssertTrue(message = ValidationErrors.DeviceWearer.OTHER_DISABILITY)
  fun isOtherDisability(): Boolean {
    if (this.disabilities != null) {
      val disabilities = Disability.getValuesFromEnumString(this.disabilities)
      if (disabilities.contains(Disability.OTHER.value)) {
        return !this.otherDisability.isNullOrBlank()
      }
    }
    return true
  }

  @AssertTrue(message = ValidationErrors.DeviceWearer.DISABILITIES_INVALID)
  fun isDisabilities(): Boolean {
    if (this.disabilities.isNullOrEmpty()) {
      return true
    }
    val submittedDisabilities = this.disabilities.split(",").map { it.trim() }.filter { it.isNotBlank() }

    return submittedDisabilities.all { submittedDisability ->
      Disability.entries.any { it.name == submittedDisability }
    }
  }

  @AssertTrue(message = ValidationErrors.DeviceWearer.SEX_REQUIRED)
  fun isSex(): Boolean = Sex.from(sex) != null

  @AssertTrue(message = ValidationErrors.DeviceWearer.GENDER_REQUIRED)
  fun isGender(): Boolean = Gender.from(gender) != null
}
