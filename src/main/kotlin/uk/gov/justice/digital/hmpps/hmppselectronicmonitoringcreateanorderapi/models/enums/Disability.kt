package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class Disability(val value: String) {
  VISION("Vision"),
  HEARING("Hearing"),
  MOBILITY("Mobility"),
  DEXTERITY("Dexterity"),
  LEARNING_UNDERSTANDING_OR_CONCENTRATING("Learning, understanding or concentrating"),
  MEMORY("Memory"),
  MENTAL_HEALTH("Mental health"),
  STAMINA_OR_BREATHING_OR_FATIGUE("Stamina or breathing or fatigue"),
  SOCIALLY_OR_BEHAVIORALLY("Socially or behaviourally"),
  OTHER("Other"),
  NONE_OF_THE_ABOVE("None of the above"),
  PREFER_NOT_TO_SAY("Prefer Not to Say"),
  ;

  companion object {
    fun getValuesFromEnumString(value: String): List<String> = value.split(",")
      .mapNotNull { disabilityName ->
        entries.find { it.name == disabilityName }?.value
      }
  }
}
