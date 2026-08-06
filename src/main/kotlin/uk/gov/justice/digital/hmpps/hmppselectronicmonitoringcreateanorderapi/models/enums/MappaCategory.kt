package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class MappaCategory(val value: String) {
  CATEGORY_ONE("Category 1"),
  CATEGORY_TWO("Category 2"),
  CATEGORY_THREE("Category 3"),
  ;

  companion object {
    fun from(value: String?): MappaCategory? = MappaCategory.entries.firstOrNull {
      it.value == value
    }
  }
}
