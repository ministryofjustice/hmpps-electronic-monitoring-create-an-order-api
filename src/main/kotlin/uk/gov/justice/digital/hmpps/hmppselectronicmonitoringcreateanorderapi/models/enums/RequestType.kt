package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

import java.time.ZonedDateTime

enum class RequestType(val value: String) {
  REQUEST("New Order"),
  VARIATION("Variation"),
  REJECTED("Rejected"),
  AMEND_ORIGINAL_REQUEST("New Order"),
  REINSTALL_AT_DIFFERENT_ADDRESS("Variation"),
  REINSTALL_DEVICE("Variation"),
  REVOCATION("Variation"),
  END_MONITORING("Variation"),
  ;

  companion object {

    val VARIATION_TYPES = listOf(
      VARIATION,
      REINSTALL_AT_DIFFERENT_ADDRESS,
      REINSTALL_DEVICE,
      REVOCATION,
      END_MONITORING,
    )

    fun getSubCategory(type: RequestType, isBail: Boolean, startDate: ZonedDateTime?): String {
      if (startDate != null &&
        startDate.toLocalDate() >= ZonedDateTime.now().toLocalDate() &&
        VARIATION_TYPES.contains(type)
      ) {
        return "SR08-Amend monitoring requirements"
      }

      return when (type) {
        VARIATION -> "SR08-Amend monitoring requirements"
        REVOCATION -> "SR21-Revocation monitoring requirements"
        END_MONITORING -> if (isBail) "SR11-Removal of devices (bail)" else "SR21-Revocation monitoring requirements"
        REINSTALL_AT_DIFFERENT_ADDRESS -> "SR05-Install monitoring equipment at an additional address"
        REINSTALL_DEVICE -> "SR04-Re-install monitoring equipment"
        else -> ""
      }
    }
  }
}
