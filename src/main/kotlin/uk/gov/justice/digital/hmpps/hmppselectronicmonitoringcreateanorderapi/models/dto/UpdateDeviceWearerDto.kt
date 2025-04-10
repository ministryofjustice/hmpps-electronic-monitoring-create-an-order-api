package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
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

  @field:Size(min = 1, message = ValidationErrors.DeviceWearer.GENDER_REQUIRED)
  val gender: String? = null,

  @field:NotNull(message = ValidationErrors.DeviceWearer.DOB_REQUIRED)
  @field:Past(message = ValidationErrors.DeviceWearer.DOB_MUST_BE_IN_PAST)
  val dateOfBirth: ZonedDateTime? = null,

  val disabilities: String? = null,

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

  @AssertTrue(message = ValidationErrors.DeviceWearer.SEX_REQUIRED)
  fun isSex(): Boolean = Sex.from(sex) != null
}
