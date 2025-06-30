package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as FmsDeviceWearer

@ActiveProfiles("test")
class DeviceWearerTest : FmsTestBase() {

  @ParameterizedTest(name = "it should map saved sex values to Serco - {0} -> {1}")
  @MethodSource("sexValues")
  fun `It should map correctly map saved sex values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(sex = savedValue),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.sex).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map saved gender values to Serco - {0} -> {1}")
  @MethodSource("genderValues")
  fun `It should map correctly map saved gender values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(gender = savedValue),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.genderIdentity).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map saved risk category values to Serco - {0} -> {1}")
  @MethodSource("getRiskCategories")
  fun `It should map correctly map saved risk category values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(),
      installationAndRisk = createInstallationAndRisk(riskCategory = savedValue),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.riskCategory!!.first().category).isEqualTo(mappedValue)
  }

  @Test
  fun `It should not map risk category to serco when risk category is NO_RISK`() {
    val order = createOrder(
      deviceWearer = createDeviceWearer(),
      installationAndRisk = createInstallationAndRisk(riskCategory = "NO_RISK"),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.riskCategory!!.count()).isEqualTo(0)
  }

  @ParameterizedTest(name = "it should map saved disability values to Serco - {0} -> {1}")
  @MethodSource("disabilityValues")
  fun `It should map correctly map saved disability values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(disabilities = savedValue),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.disability!!.first().disability).isEqualTo(mappedValue)
  }

  companion object {
    @JvmStatic
    fun sexValues() = listOf(
      Arguments.of("MALE", "Male"),
      Arguments.of("FEMALE", "Female"),
      Arguments.of("PREFER_NOT_TO_SAY", "Prefer Not to Say"),
      Arguments.of("UNKNOWN", "Prefer Not to Say"),
    )

    @JvmStatic
    fun genderValues() = listOf(
      Arguments.of("MALE", "Male"),
      Arguments.of("FEMALE", "Female"),
      Arguments.of("NON_BINARY", "Non-Binary"),
      Arguments.of("PREFER_TO_SELF_DESCRIBE", "Prefer to self-describe"),
      Arguments.of("NOT_ABLE_TO_PROVIDE_THIS_INFORMATION", ""),
    )

    @JvmStatic
    fun getRiskCategories() = listOf(
      Arguments.of("THREATS_OF_VIOLENCE", "Threats of Violence"),
      Arguments.of("SEXUAL_OFFENCES", "Sexual Offences"),
      Arguments.of("RISK_TO_GENDER", "Risk to Specific Gender"),
      Arguments.of("RACIAL_ABUSE_OR_THREATS", "Racial Abuse or Threats"),
      Arguments.of("DIVERSITY_CONCERNS", "Diversity Concerns (mental health issues, learning difficulties etc.)"),
      Arguments.of("DANGEROUS_ANIMALS", "Dangerous Dogs/Pets at Premises"),
      Arguments.of("IOM", "Is the Subject managed through IOM?"),
      Arguments.of("SAFEGUARDING_ISSUE", "Safeguarding Issues"),
      Arguments.of("SAFEGUARDING_ADULT", "Safeguarding Adult"),
      Arguments.of("SAFEGUARDING_CHILD", "Safeguarding Child"),
      Arguments.of("SAFEGUARDING_DOMESTIC_ABUSE", "Safeguarding Domestic Abuse"),
      Arguments.of("OTHER_OCCUPANTS", "Other occupants who pose a risk to staff"),
      Arguments.of("OTHER_RISKS", "Other known Risks"),
      Arguments.of("HOMOPHOBIC_VIEWS", "Is there evidence known to the subject having homophobic views"),
      Arguments.of("UNDER_18", "Under 18 living at property"),
    )

    @JvmStatic
    fun disabilityValues() = listOf(
      Arguments.of("VISION", "Vision"),
      Arguments.of("HEARING", "Hearing"),
      Arguments.of("MOBILITY", "Mobility"),
      Arguments.of("DEXTERITY", "Dexterity"),
      Arguments.of("SKIN_CONDITION", "Skin condition"),
      Arguments.of("LEARNING_UNDERSTANDING_CONCENTRATING", "Learning, understanding or concentrating"),
      Arguments.of("MEMORY", "Memory"),
      Arguments.of("MENTAL_HEALTH", "Mental health"),
      Arguments.of("STAMINA_BREATHING_FATIGUE", "Stamina or breathing or fatigue"),
      Arguments.of("SOCIAL_BEHAVIOURAL", "Socially or behaviourally"),
      Arguments.of("OTHER", "Other"),
      Arguments.of("NONE", "None of the above"),
      Arguments.of("PREFER_NOT_TO_SAY", "Prefer Not to Say"),
    )
  }
}
