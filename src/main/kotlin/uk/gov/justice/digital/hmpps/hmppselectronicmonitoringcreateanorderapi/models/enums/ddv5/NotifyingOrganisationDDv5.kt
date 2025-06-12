package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class NotifyingOrganisationDDv5(val value: String) {
  CIVIL_COUNTY_COURT("Civil County Court"),
  CROWN_COURT("Crown Court"),
  MAGISTRATES_COURT("Magistrates Court"),
  MILITARY_COURT("Military Court"),
  PRISON("Prison"),
  HOME_OFFICE("Home Office"),
  SCOTTISH_COURT("Scottish Court"),
  FAMILY_COURT("Family Court"),
  PROBATION("Probation"),
  YOUTH_COURT("Youth Court"),
  YOUTH_CUSTODY_SERVICE("Youth Custody Service"),
  ;

  companion object {
    fun from(value: String?): NotifyingOrganisationDDv5? = NotifyingOrganisationDDv5.entries.firstOrNull {
      it.name == value
    }
  }
}
