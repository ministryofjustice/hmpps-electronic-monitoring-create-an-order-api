package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class Offence(val value: String) {
  VIOLENCE_AGAINST_THE_PERSON("Violence against the person"),
  SEXUAL_OFFENCES("Sexual offences"),
  ROBBERY("Robbery"),
  THEFT_OFFENCES("Theft Offences"),
  CRIMINAL_DAMAGE_AND_ARSON("Criminal damage and arson"),
  DRUG_OFFENCES("Drug offences"),
  POSSESSION_OF_WEAPONS("Possession of weapons"),
  PUBLIC_ORDER_OFFENCES("Public order offences"),
  MISCELLANEOUS_CRIMES_AGAINST_SOCIETY("Miscellaneous crimes against society"),
  FRAUD_OFFENCES("Fraud Offences"),
  SUMMARY_NON_MOTORING("Summary Non-Motoring"),
  SUMMARY_MOTORING("Summary motoring"),
  OFFENCE_NOT_RECORDED("Offence not recorded"),
  NO_OFFENCE_COMMITTED(""),
  ;

  companion object {
    fun from(value: String?): Offence? = Offence.entries.firstOrNull {
      it.name == value
    }
  }
}
