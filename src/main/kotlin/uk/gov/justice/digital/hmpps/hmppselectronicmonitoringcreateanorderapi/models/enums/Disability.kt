package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class Disability(val value: String) {
  VISION("Vision"),
  HEARING("Hearing"),
  MOBILITY("Mobility"),
  DEXTERITY("Dexterity"),
  SKIN_CONDITION("Skin Condition"),
  LEARNING_UNDERSTANDING_CONCENTRATING("Learning, understanding or concentrating"),
  MEMORY("Memory"),
  MENTAL_HEALTH("Mental health"),
  STAMINA_BREATHING_FATIGUE("Stamina or breathing or fatigue"),
  SOCIAL_BEHAVIOURAL("Socially or behaviourally"),
  OTHER("Other"),
  NO_LISTED_CONDITION(""),
  NONE("None of the above"),
  PREFER_NOT_TO_SAY("Prefer Not to Say"),
  ;

  companion object {
    fun getValuesFromEnumString(value: String): List<String> = value.split(",")
      .filter { it != "NO_LISTED_CONDITION" }
      .mapNotNull { disabilityName ->
        entries.find { it.name == disabilityName }?.value
      }
  }
}
