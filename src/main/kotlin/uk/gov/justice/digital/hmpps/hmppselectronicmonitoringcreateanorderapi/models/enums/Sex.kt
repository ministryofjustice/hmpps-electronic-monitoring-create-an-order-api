package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class Sex(val value: String) {
  MALE("Male"),
  FEMALE("Female"),
  PREFER_NOT_TO_SAY("Prefer Not to Say"),
  UNKNOWN("Unknown"),
  ;

  companion object {
    fun from(value: String?): Sex? = Sex.entries.firstOrNull {
      it.name == value
    }
  }
}
