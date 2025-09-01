package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.enums.ddv4

enum class Sex(val value: String) {
  MALE("Male"),
  FEMALE("Female"),
  PREFER_NOT_TO_SAY("Prefer Not to Say"),
  UNKNOWN("Prefer Not to Say"),
  ;

  companion object {
    fun from(value: String?): Sex? = entries.firstOrNull {
      it.name == value
    }
  }
}
