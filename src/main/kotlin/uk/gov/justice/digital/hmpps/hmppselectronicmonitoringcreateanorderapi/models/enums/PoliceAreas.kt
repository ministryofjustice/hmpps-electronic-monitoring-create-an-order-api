package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class PoliceAreas(val value: String) {
  AVON_AND_SOMERSET("Avon and Somerset Constabulary"),
  BEDFORDSHIRE("Bedfordshire Police"),
  CHESHIRE("Cheshire Constabulary"),
  CITY_OF_LONDON("City of London Police"),
  CUMBRIA("Cumbria Constabulary"),
  DERBYSHIRE("Derbyshire Police"),
  DURHAM("Durham Constabulary"),
  ESSEX("Essex Police"),
  GLOUCESTERSHIRE("Gloucestershire Constabulary"),
  GWENT("Gwent Police"),
  HAMPSHIRE("Hampshire Constabulary"),
  HERTFORDSHIRE("Hertfordshire Constabulary"),
  HUMBERSIDE("Humberside Police"),
  KENT("Kent Police"),
  METROPOLITAN_POLICE("Metropolitan Police Service"),
  NORTH_WALES("North Wales Police"),
  NOTTINGHAMSHIRE("Nottinghamshire Police"),
  SUSSEX("Sussex Police"),
  WEST_MIDLANDS("West Midlands Police"),
  DIFFERENT_POLICE_AREA("The device wearer's release address is in a different police force area"),
  ;

  companion object {
    fun from(value: String?): PoliceAreas? = PoliceAreas.entries.firstOrNull {
      it.name == value
    }

    fun fromValue(value: String?): PoliceAreas? {
      if (value.isNullOrBlank()) return DIFFERENT_POLICE_AREA

      return PoliceAreas.entries.firstOrNull {
        it.value == value
      }
    }
  }
}
