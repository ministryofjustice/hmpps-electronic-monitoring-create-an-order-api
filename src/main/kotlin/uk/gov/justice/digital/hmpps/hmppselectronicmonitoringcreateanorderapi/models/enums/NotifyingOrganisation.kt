package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class NotifyingOrganisation(val value: String) {
  CROWN_COURT("Crown Court"),
  MAGISTRATES_COURT("Magistrates Court"),
  PRISON("Prison"),
  HOME_OFFICE("Home Office"),
  SCOTTISH_COURT("Scottish Court"),
  FAMILY_COURT("Family Court"),
  PROBATION("Probation"),
  ;

  companion object {
    fun from(value: String?): NotifyingOrganisation? {
      return NotifyingOrganisation.entries.firstOrNull {
        it.value == value
      }
    }
  }
}
