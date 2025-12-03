package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class RequestType(val value: String) {
  REQUEST("New Order"),
  VARIATION("Variation"),
  REJECTED("Rejected"),
  AMEND_ORIGINAL_REQUEST("New Order"),
  REINSTALL_AT_DIFFERENT_ADDRESS("Variation"),
  REINSTALL_DEVICE("Variation"),
  REVOCATION("Variation"),
  ;

  companion object {

    val VARIATION_TYPES = listOf(
      VARIATION,
      REINSTALL_AT_DIFFERENT_ADDRESS,
      REINSTALL_DEVICE,
      REVOCATION,
    )
  }
}
