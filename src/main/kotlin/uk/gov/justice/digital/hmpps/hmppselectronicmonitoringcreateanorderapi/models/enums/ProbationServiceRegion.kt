package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class ProbationServiceRegion(val value: String) {
  NORTH_EAST("North East"),
  NORTH_WEST("North West"),
  YORKSHIRE_AND_THE_HUMBER("Yorkshire and the Humber"),
  GREATER_MANCHESTER("Greater Manchester"),
  EAST_MIDLANDS("East Midlands"),
  WALES("Wales"),
  WEST_MIDLANDS("West Midlands"),
  EAST_OF_ENGLAND("East of England"),
  SOUTH_WEST("South West"),
  SOUTH_CENTRAL("South Central"),
  LONDON("London"),
  KENT_SURREY_SUSSEX("Kent, Surrey & Sussex"),
  ;

  companion object {
    fun from(value: String?): ProbationServiceRegion? {
      return ProbationServiceRegion.entries.firstOrNull {
        it.name == value
      }
    }
  }
}
