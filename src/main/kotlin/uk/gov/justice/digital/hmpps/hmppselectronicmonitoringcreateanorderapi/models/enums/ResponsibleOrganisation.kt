package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class ResponsibleOrganisation(val value: String) {
  YJS("YJS"),
  YCS("YCS"),
  PROBATION("Probation"),
  FIELD_MONITORING_SERVICE("Field Monitoring Service"),
  HOME_OFFICE("Home Office"),
  POLICE("Police"),
  ;

  companion object {
    fun from(value: String?): ResponsibleOrganisation? {
      return ResponsibleOrganisation.entries.firstOrNull {
        it.toString() == value
      }
    }
  }
}
