package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [AtLeastOneSelectedValidator::class])
@MustBeDocumented
annotation class AtLeastOneSelected(
  val message: String = "At least one option must be selected.",
  val fieldNames: Array<String>,
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class AtLeastOneSelectedValidator : ConstraintValidator<AtLeastOneSelected, Any> {
  private lateinit var fieldNames: Array<String>
  private lateinit var message: String

  override fun initialize(constraintAnnotation: AtLeastOneSelected) {
    fieldNames = constraintAnnotation.fieldNames
    message = constraintAnnotation.message
  }
  override fun isValid(objectToValidate: Any, context: ConstraintValidatorContext): Boolean {
    val fieldValues = fieldNames.mapNotNull { fieldName ->
      getFieldValue(objectToValidate, fieldName)
    }

    return fieldValues.any { it }
  }

  private fun getFieldValue(objectToValidate: Any, fieldName: String): Boolean? {
    val property = objectToValidate::class.memberProperties
      .firstOrNull { it.name == fieldName } as? KProperty1<Any, *>

    return property?.get(objectToValidate) as? Boolean
  }
}
