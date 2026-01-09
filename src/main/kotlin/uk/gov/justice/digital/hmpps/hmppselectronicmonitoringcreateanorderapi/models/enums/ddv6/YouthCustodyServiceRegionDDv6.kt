package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6

enum class YouthCustodyServiceRegionDDv6(val value: String) {
  SOUTH_WEST_AND_SOUTH_CENTRAL("South West and South Central"),
  EAST_AND_SOUTH_EAST("East and South East"),
  LONDON("London"),
  MIDLANDS("Midlands"),
  NORTH_EAST_AND_CUMBRIA("North East and Cumbria"),
  NORTH_WEST("North West"),
  WALES("Wales"),
  YORKSHIRE_AND_HUMBERSIDE("Yorkshire and Humberside"),
  ;

  companion object {
    fun from(value: String?): YouthCustodyServiceRegionDDv6? = YouthCustodyServiceRegionDDv6.entries.firstOrNull {
      it.name == value
    }
  }
}
