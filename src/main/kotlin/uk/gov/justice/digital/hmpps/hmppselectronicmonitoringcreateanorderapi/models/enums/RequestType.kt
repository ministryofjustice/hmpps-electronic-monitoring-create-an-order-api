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

    fun getSubCategory(type: RequestType, isBail: Boolean): String = when (type) {
      VARIATION -> "SR08-Amend monitoring requirements"
      REVOCATION -> if (isBail) "SR11-Removal of devices (bail)" else "SR21-Revocation monitoring requirements"
      REINSTALL_AT_DIFFERENT_ADDRESS -> "SR05-Install monitoring equipment at an additional address"
      REINSTALL_DEVICE -> "SR04-Re-install monitoring equipment"
      else -> ""
    }
  }
}
