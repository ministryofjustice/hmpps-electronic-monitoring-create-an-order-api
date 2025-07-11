package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

data class UpdateVariationDetailsDto(
  @field:NotEmpty(message = ValidationErrors.VariationDetails.TYPE_REQUIRED)
  val variationType: String = "",

  @field:NotEmpty(message = ValidationErrors.VariationDetails.DATE_REQUIRED)
  val variationDate: String = "",

  @field:NotEmpty(message = ValidationErrors.VariationDetails.DETAILS_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.VariationDetails.DETAIL_TOO_LONG)
  val variationDetails: String = "",

) {
  @AssertTrue(message = ValidationErrors.VariationDetails.TYPE_MUST_BE_VALID)
  fun isVariationType(): Boolean {
    // Prevent additional error being generated for empty string
    if (variationType == "") {
      return true
    }

    for (entry in VariationType.entries) {
      if (entry.name == variationType) {
        return true
      }
    }
    return false
  }

  @AssertTrue(message = ValidationErrors.VariationDetails.DATE_MUST_BE_VALID)
  fun isVariationDate(): Boolean {
    // Prevent additional error being generated for empty string
    if (variationDate == "") {
      return true
    }

    try {
      ZonedDateTime.parse(variationDate)
      return true
    } catch (e: DateTimeParseException) {
      return false
    }
  }
}
