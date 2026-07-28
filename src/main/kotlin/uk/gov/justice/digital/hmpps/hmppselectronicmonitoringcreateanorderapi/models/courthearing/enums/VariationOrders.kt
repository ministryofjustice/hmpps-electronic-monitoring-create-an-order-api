package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.enums

enum class VariationOrders(val id: String) {
  // Variation of bail conditions
  RCBV("90d8268d-cc6a-4a09-bdb3-ddf8ea8ef2f9"),
  ;

  companion object {
    fun contains(id: String?) = VariationOrders.entries.any { it.id == id }
  }
}
