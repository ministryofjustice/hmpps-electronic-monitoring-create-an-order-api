package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@MustBeDocumented
@Constraint(validatedBy = [TimePatternValidator::class])
annotation class ValidTime(
  val message: String = "Time is in an incorrect format. Expected 24 hour time in format: HH:mm",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class TimePatternValidator : ConstraintValidator<ValidTime, String> {
  private val timePattern = Regex("^([01][0-9]|2[0-3]):([0-5][0-9])\$")

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) return true
    if (value.isBlank()) return false
    return this.isValidPhoneNumber(value)
  }
  private fun isValidPhoneNumber(value: String): Boolean {
    return try {
      // If no + prefix defaults to uk
      val countryCode = if (value.startsWith("+")) "" else "GB"
      val numberUtil = PhoneNumberUtil.getInstance()
      val phoneNumber = numberUtil.parse(value, countryCode)
      numberUtil.isValidNumber(phoneNumber)
    } catch (e: NumberParseException) {
      false
    }
  }
}