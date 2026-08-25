package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotBlank

data class StoreDeviceWearerDetailsDto(
  @field:NotBlank(message = "organisationSearchId must not be blank")
  val organisationSearchId: String? = null,
)
