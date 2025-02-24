package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class CrownCourt(val value: String) {
  YORK_CROWN_COURT("York Crown Court"),
  AYLESBURY_CROWN_COURT("Aylesbury Crown Court"),
  BIRMINGHAM_CROWN_COURT("Birmingham Crown Court"),
  BOLTON_CROWN_COURT("Bolton Crown Court"),
  BOURNEMOUTH_CROWN_COURT("Bournemouth Crown Court"),
  BRISTOL_CROWN_COURT("Bristol Crown Court"),
  CAMBRIDGE_CROWN_COURT("Cambridge Crown Court"),
  CARDIFF_CROWN_COURT("Cardiff Crown Court"),
  CHELMSFORD_CROWN_COURT("Chelmsford Crown Court"),
  CHESTER_CROWN_COURT("Chester Crown Court"),
  CROYDON_CROWN_COURT("Croydon Crown Court"),
  DURHAM_CROWN_COURT("Durham Crown Court"),
  GLOUCESTER_CROWN_COURT("Gloucester Crown Court"),
  GUILDFORD_CROWN_COURT("Guildford Crown Court"),
  HARROW_CROWN_COURT("Harrow Crown Court"),
  HEREFORD_CROWN_COURT("Hereford Crown Court"),
  INNER_LONDON_CROWN_COURT("Inner London Crown Court"),
  IPSWICH_CROWN_COURT("Ipswich Crown Court"),
  ISLEWORTH_CROWN_COURT("Isleworth Crown Court"),
  KINGS_LYNN_CROWN_COURT("King's Lynn Crown Court"),
  KINGSTON_UPON_THAMES_CROWN_COURT("Kingston upon Thames Crown Court"),
  LANCASTER_CROWN_COURT("Lancaster Crown Court"),
  LEICESTER_CROWN_COURT("Leicester Crown Court"),
  LINCOLN_CROWN_COURT("Lincoln Crown Court"),
  LIVERPOOL_CROWN_COURT("Liverpool Crown Court"),
  LUTON_CROWN_COURT("Luton Crown Court"),
  MANCHESTER_CROWN_COURT_CROWN_SQUARE("Manchester Crown Court (Crown Square)"),
  MANCHESTER_CROWN_COURT_MINSHULL_ST("Manchester Crown Court (Minshull St)"),
  NEWCASTLE_UPON_TYNE_CROWN_COURT("Newcastle upon Tyne Crown Court"),
  NEWPORT_SOUTH_WALES_CROWN_COURT("Newport (South Wales) Crown Court"),
  NOTTINGHAM_CROWN_COURT("Nottingham Crown Court"),
  PRESTON_CROWN_COURT("Preston Crown Court"),
  READING_CROWN_COURT("Reading Crown Court"),
  SHREWSBURY_CROWN_COURT("Shrewsbury Crown Court"),
  SNARESBROOK_CROWN_COURT("Snaresbrook Crown Court"),
  SOUTHEND_CROWN_COURT("Southend Crown Court"),
  SOUTHWARK_CROWN_COURT("Southwark Crown Court"),
  ST_ALBANS_CROWN_COURT("St Albans Crown Court"),
  SWANSEA_CROWN_COURT("Swansea Crown Court"),
  WARRINGTON_CROWN_COURT("Warrington Crown Court"),
  WOOD_GREEN_CROWN_COURT("Wood Green Crown Court"),
  WOOLWICH_CROWN_COURT("Woolwich Crown Court"),
  ;

  companion object {
    fun from(value: String?): CrownCourt? = CrownCourt.entries.firstOrNull {
      it.name == value
    }
  }
}
