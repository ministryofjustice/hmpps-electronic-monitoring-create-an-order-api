package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.OrderTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as FmsDeviceWearer

@ActiveProfiles("test")
class DeviceWearerTest : OrderTestBase() {

  @Test
  fun `It should map primary address to Serco`() {
    val mockAddress = createAddress(
      addressLine1 = "Primary Line 1",
      addressLine2 = "Primary Line 2",
      addressLine3 = "Primary Line 3",
      addressLine4 = "Primary Line 4",
      postcode = "Primary Post code",
      addressType = AddressType.PRIMARY,
    )
    val order = createOrder(
      addresses = mutableListOf(mockAddress),
    )

    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.address1).isEqualTo(mockAddress.addressLine1)
    assertThat(fmsDeviceWearer.address2).isEqualTo(mockAddress.addressLine2)
    assertThat(fmsDeviceWearer.address3).isEqualTo(mockAddress.addressLine3)
    assertThat(fmsDeviceWearer.address4).isEqualTo(mockAddress.addressLine4)
    assertThat(fmsDeviceWearer.addressPostCode).isEqualTo(mockAddress.postcode)
  }

  @Test
  fun `It should map secondary address to Serco`() {
    val primaryAddress = createAddress(
      addressLine1 = "Primary Line 1",
      addressLine2 = "Primary Line 2",
      addressLine3 = "Primary Line 3",
      addressLine4 = "Primary Line 4",
      postcode = "Primary Post code",
      addressType = AddressType.PRIMARY,
    )
    val mockAddress = createAddress(
      addressLine1 = "Secondary Line 1",
      addressLine2 = "Secondary Line 2",
      addressLine3 = "Secondary Line 3",
      addressLine4 = "Secondary Line 4",
      postcode = "Secondary Post code",
      addressType = AddressType.SECONDARY,
    )
    val order = createOrder(
      addresses = mutableListOf(primaryAddress, mockAddress),
    )

    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.secondaryAddress1).isEqualTo(mockAddress.addressLine1)
    assertThat(fmsDeviceWearer.secondaryAddress2).isEqualTo(mockAddress.addressLine2)
    assertThat(fmsDeviceWearer.secondaryAddress3).isEqualTo(mockAddress.addressLine3)
    assertThat(fmsDeviceWearer.secondaryAddress4).isEqualTo(mockAddress.addressLine4)
    assertThat(fmsDeviceWearer.secondaryAddressPostCode).isEqualTo(mockAddress.postcode)
  }

  @Test
  fun `It should map tertiary address to Serco`() {
    val primaryAddress = createAddress(
      addressLine1 = "Primary Line 1",
      addressLine2 = "Primary Line 2",
      addressLine3 = "Primary Line 3",
      addressLine4 = "Primary Line 4",
      postcode = "Primary Post code",
      addressType = AddressType.PRIMARY,
    )
    val mockAddress = createAddress(
      addressLine1 = "TERTIARY Line 1",
      addressLine2 = "TERTIARY Line 2",
      addressLine3 = "TERTIARY Line 3",
      addressLine4 = "TERTIARY Line 4",
      postcode = "TERTIARY Post code",
      addressType = AddressType.TERTIARY,
    )
    val order = createOrder(
      addresses = mutableListOf(primaryAddress, mockAddress),
    )

    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.tertiaryAddress1).isEqualTo(mockAddress.addressLine1)
    assertThat(fmsDeviceWearer.tertiaryAddress2).isEqualTo(mockAddress.addressLine2)
    assertThat(fmsDeviceWearer.tertiaryAddress3).isEqualTo(mockAddress.addressLine3)
    assertThat(fmsDeviceWearer.tertiaryAddress4).isEqualTo(mockAddress.addressLine4)
    assertThat(fmsDeviceWearer.tertiaryAddressPostCode).isEqualTo(mockAddress.postcode)
  }

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

  @Test
  fun `It should not send NO_LISTED_CONDITION option to Serco`() {
    val order = createOrder(
      deviceWearer = createDeviceWearer(disabilities = "NO_LISTED_CONDITION"),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.disability!!.count()).isEqualTo(0)
  }

  @Test
  fun `It should map cepr to home office reference number`() {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
      deviceWearer = createDeviceWearer(
        homeOfficeReferenceNumber = "",
        complianceAndEnforcementPersonReference = "CC123",
      ),
      interestedParties = createInterestedParty(
        notifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE.name,
      ),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.homeOfficeReferenceNumber).isEqualTo("CC123")
  }

  @Test
  fun `It should map home office reference number when cepr is null`() {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
      deviceWearer = createDeviceWearer(
        homeOfficeReferenceNumber = "CC123",
        complianceAndEnforcementPersonReference = null,
      ),
      interestedParties = createInterestedParty(
        notifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE.name,
      ),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.homeOfficeReferenceNumber).isEqualTo("CC123")
  }

  @Test
  fun `It should map home office reference number when cepr is empty string`() {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
      deviceWearer = createDeviceWearer(
        homeOfficeReferenceNumber = "CC123",
        complianceAndEnforcementPersonReference = "",
      ),
      interestedParties = createInterestedParty(
        notifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE.name,
      ),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.homeOfficeReferenceNumber).isEqualTo("CC123")
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
      Arguments.of("IOM", "Device Wearer managed through IOM?"),
      Arguments.of("SAFEGUARDING_ISSUE", "Safeguarding Issues"),
      Arguments.of("SAFEGUARDING_ADULT", "Safeguarding Adult"),
      Arguments.of("SAFEGUARDING_CHILD", "Safeguarding Child"),
      Arguments.of("SAFEGUARDING_DOMESTIC_ABUSE", "Safeguarding Domestic Abuse"),
      Arguments.of("OTHER_OCCUPANTS", "Other occupants who pose a risk to staff"),
      Arguments.of("OTHER_RISKS", "Other known Risks"),
      Arguments.of("HOMOPHOBIC_VIEWS", "Evidence known to the Device Wearer having homophobic views"),
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
