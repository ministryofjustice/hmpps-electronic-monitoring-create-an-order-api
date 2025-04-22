package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class Gender(val value: String) {
  MALE("Male"),
  FEMALE("Female"),
  NON_BINARY("Non-Binary"),
  PREFER_TO_SELF_DESCRIBE("Prefer to self-describe"),
  NOT_ABLE_TO_PROVIDE_THIS_INFORMATION(""),
  ;

  companion object {
    fun from(value: String?): Gender? = Gender.entries.firstOrNull {
      it.name == value
    }
  }
}
