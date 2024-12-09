package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

data class UpdateIdentityNumbersDto(
  val nomisId: String? = "",

  val pncId: String? = "",

  val deliusId: String? = "",

  val prisonNumber: String? = "",

  var homeOfficeReferenceNumber: String? = "",
)
