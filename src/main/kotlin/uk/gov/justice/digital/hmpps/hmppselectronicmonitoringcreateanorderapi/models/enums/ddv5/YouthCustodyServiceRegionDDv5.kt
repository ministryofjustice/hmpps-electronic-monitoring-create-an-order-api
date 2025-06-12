package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class YouthCustodyServiceRegionDDv5(val value: String) {
  CENTRAL("Central"),
  EAST_AND_SOUTH_EAST("East and South East"),
  LONDON("London"),
  MIDLANDS("Midlands"),
  NORTH_EAST_AND_CUMBRIA("North East and Cumbria"),
  NORTH_WEST("North West"),
  WALES("Wales"),
  YORKSHIRE_AND_HUMBERSIDE("Yorkshire and Humberside"),
  ;

  companion object {
    fun from(value: String?): YouthCustodyServiceRegionDDv5? = YouthCustodyServiceRegionDDv5.entries.firstOrNull {
      it.name == value
    }
  }
}
