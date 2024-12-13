package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

data class UpdateVariationDetailsDto(
  @field:NotEmpty(message = "Variation type is required")
  val variationType: String = "",

  @field:NotEmpty(message = "Variation date is required")
  val variationDate: String = "",
) {
  @AssertTrue(message = "Variation type must be a valid variation type")
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

  @AssertTrue(message = "Variation date must be a valid date")
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
