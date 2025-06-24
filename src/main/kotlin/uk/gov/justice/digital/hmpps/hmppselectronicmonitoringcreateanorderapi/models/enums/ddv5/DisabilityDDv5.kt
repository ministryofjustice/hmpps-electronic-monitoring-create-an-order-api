package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv5

enum class DisabilityDDv5(val value: String) {
  VISION("Vision"),
  HEARING("Hearing"),
  MOBILITY("Mobility"),
  DEXTERITY("Dexterity"),
  SKIN_CONDITION("Skin condition"),
  LEARNING_UNDERSTANDING_CONCENTRATING("Learning, understanding or concentrating"),
  MEMORY("Memory"),
  MENTAL_HEALTH("Mental health"),
  STAMINA_BREATHING_FATIGUE("Stamina or breathing or fatigue"),
  SOCIAL_BEHAVIOURAL("Socially or behaviourally"),
  OTHER("Other"),
  NONE("None of the above"),
  PREFER_NOT_TO_SAY("Prefer Not to Say"),
  ;

  companion object {
    fun getValuesFromEnumString(value: String): List<String> = value.split(",")
      .mapNotNull { disabilityName ->
        entries.find { it.name == disabilityName }?.value
      }
  }
}
