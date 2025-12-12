package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class PoliceAreas(val value: String) {
  AVON_AND_SOMERSET("Avon and Somerset"),
  BEDFORDSHIRE("Bedfordshire"),
  CHESHIRE("Cheshire"),
  CITY_OF_LONDON("City of London"),
  CUMBRIA("Cumbria"),
  DERBYSHIRE("Derbyshire"),
  DURHAM("Durham"),
  ESSEX("Essex"),
  GLOUCESTERSHIRE("Gloucestershire"),
  GWENT("Gwent"),
  HAMPSHIRE("Hampshire"),
  HERTFORDSHIRE("Hertfordshire"),
  HUMBERSIDE("Humberside"),
  KENT("Kent"),
  METROPOLITAN_POLICE("Metropolitan Police"),
  NORTH_WALES("North Wales"),
  NOTTINGHAMSHIRE("Nottinghamshire"),
  SUSSEX("Sussex"),
  WEST_MIDLANDS("West Midlands"),
  DIFFERENT_POLICE_AREA("The device wearer's release address is in a different police force area"),
  ;

  companion object {
    fun from(value: String?): PoliceAreas? = PoliceAreas.entries.firstOrNull {
      it.name == value
    }
  }
}
