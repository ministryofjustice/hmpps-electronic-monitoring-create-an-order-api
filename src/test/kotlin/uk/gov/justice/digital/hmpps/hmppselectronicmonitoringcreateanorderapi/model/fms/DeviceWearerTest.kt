package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.OrderTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.DeviceWearerFieldChangeArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.DeviceWearerNegativeFieldArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.FieldChangeCase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DetailsOfInstallation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRiskCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.compareTo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as FmsDeviceWearer

@ActiveProfiles("test")
class DeviceWearerTest : OrderTestBase() {
  private val featureFlags = FeatureFlags(ddV6CourtMappings = false, dataDictionaryVersion = DataDictionaryVersion.DDV6)

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

    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

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

    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

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

    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

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
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

    assertThat(fmsDeviceWearer.sex).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map saved gender values to Serco - {0} -> {1}")
  @MethodSource("genderValues")
  fun `It should map correctly map saved gender values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(gender = savedValue),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

    assertThat(fmsDeviceWearer.genderIdentity).isEqualTo(mappedValue)
  }

  @ParameterizedTest(name = "it should map saved risk category values to Serco - {0} -> {1}")
  @MethodSource("getRiskCategories")
  fun `It should map correctly map saved risk category values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(),
      installationAndRisk = createInstallationAndRisk(riskCategory = savedValue),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

    assertThat(fmsDeviceWearer.riskCategory!!.first().category).isEqualTo(mappedValue)
  }

  @Test
  fun `It should not map risk category to serco when risk category is NO_RISK`() {
    val order = createOrder(
      deviceWearer = createDeviceWearer(),
      installationAndRisk = createInstallationAndRisk(riskCategory = "NO_RISK"),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

    assertThat(fmsDeviceWearer.riskCategory!!.count()).isEqualTo(0)
  }

  @Test
  fun `It should not send NO_LISTED_CONDITION option to Serco`() {
    val order = createOrder(
      deviceWearer = createDeviceWearer(disabilities = "NO_LISTED_CONDITION"),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

    assertThat(fmsDeviceWearer.disability!!.count()).isEqualTo(0)
  }

  @Test
  fun `It should map cepr correctly and not map it to home office reference number`() {
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
    val ddv6EnabledFlags = FeatureFlags(dataDictionaryVersion = DataDictionaryVersion.DDV6, ddV6CourtMappings = true)
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, ddv6EnabledFlags)

    assertThat(fmsDeviceWearer.homeOfficeReferenceNumber).isEqualTo("")
    assertThat(fmsDeviceWearer.complianceAndEnforcementPersonReference).isEqualTo("CC123")
  }

  @Test
  fun `It should map home office reference number as blank string and not map from cepr`() {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
      deviceWearer = createDeviceWearer(
        homeOfficeReferenceNumber = "CC123",
        complianceAndEnforcementPersonReference = "DD123",
      ),
      interestedParties = createInterestedParty(
        notifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE.name,
      ),
    )
    val ddv6EnabledFlags = FeatureFlags(dataDictionaryVersion = DataDictionaryVersion.DDV6, ddV6CourtMappings = true)
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, ddv6EnabledFlags)

    assertThat(fmsDeviceWearer.homeOfficeReferenceNumber).isEqualTo("")
  }

  @ParameterizedTest(name = "it should map saved disability values to Serco - {0} -> {1}")
  @MethodSource("disabilityValues")
  fun `It should map correctly map saved disability values to Serco`(savedValue: String, mappedValue: String) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(disabilities = savedValue),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

    assertThat(fmsDeviceWearer.disability!!.first().disability).isEqualTo(mappedValue)
  }

  @Test
  fun `It should map risk data from detailsOfInstallation`() {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
      deviceWearer = createDeviceWearer(),
    )

    order.installationAndRisk = null

    order.detailsOfInstallation =
      DetailsOfInstallation(
        versionId = order.getCurrentVersion().id,
        riskDetails = "History of violence",
        riskCategory = arrayOf("THREATS_OF_VIOLENCE"),
      )

    val featureFlags = FeatureFlags(dataDictionaryVersion = DataDictionaryVersion.DDV6, ddV6CourtMappings = true)
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

    assertThat(fmsDeviceWearer.riskDetails).isEqualTo("History of violence")
    assertThat(fmsDeviceWearer.riskCategory).isNotNull
    assertThat(fmsDeviceWearer.riskCategory).hasSize(1)
    assertThat(fmsDeviceWearer.riskCategory!!.first().category).isEqualTo("Threats of Violence")
  }

  @Test
  fun `It should map mappa category correctly for serco`() {
    val order = createOrder(
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
    )

    order.mappa = Mappa(
      versionId = order.getCurrentVersion().id,
      level = MappaLevel.MAPPA_ONE,
      category = MappaCategory.CATEGORY_ONE,
      isMappa = YesNoUnknown.YES,
    )

    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

    assertThat(fmsDeviceWearer.mappaCaseType).isEqualTo("Category 1")
    assertThat(fmsDeviceWearer.mappa).isEqualTo("MAPPA 1")
  }

  @Test
  fun `It should map all names`() {
    val order =
      createOrder(deviceWearer = createDeviceWearer(firstName = "First", middleName = "Middle", lastName = "Last"))

    val result = FmsDeviceWearer.fromCemoOrder(order, featureFlags)

    assertThat(result.firstName).isEqualTo("First")
    assertThat(result.middleName).isEqualTo("Middle")
    assertThat(result.lastName).isEqualTo("Last")
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
      Arguments.of("SKIN_CONDITION", "Skin Condition"),
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

  @Nested
  @DisplayName("CompareTo")
  inner class CompareTo {

    private fun baselineWearer() = DeviceWearer(
      firstName = "John",
      lastName = "Doe",
      alias = "JD",
      dateOfBirth = "1990-01-01",
      sex = "Male",
      genderIdentity = "Male",
      address1 = "123 Main St",
      secondaryAddress1 = "Flat 2",
      phoneNumber = "07000000000",
      noFixedAddress = "false",
      mappa = "Yes",
      interpreterRequired = "No",
      responsibleAdultRequired = "false",
      parentPhoneNumber = "07111111111",
      disability = listOf(Disability("Visual")),
      riskCategory = listOf(FmsRiskCategory("High")),
    )

    @Test
    fun `returns empty list when no fields change`() {
      val old = baselineWearer()
      val updated = baselineWearer()

      val result = old.compareTo(updated)

      assertThat(result).isEmpty()
    }


    @Test
    fun `returns message when value is added`() {
      val old = baselineWearer().apply {
        phoneNumber = ""
      }
      val updated = baselineWearer().apply {
        phoneNumber = "07999999999"
      }

      val result = old.compareTo(updated)

      assertThat(result)
        .containsExactly("Device wearer's phone number has changed")
    }

    @Test
    fun `returns message when value is deleted`() {
      val old = baselineWearer()
      val updated = baselineWearer().apply {
        phoneNumber = ""
      }

      val result = old.compareTo(updated)

      assertThat(result)
        .containsExactly("Device wearer's phone number has changed")
    }

    @Test
    fun `returns message when list content changes`() {
      val old = baselineWearer()
      val updated = baselineWearer().apply {
        disability = listOf(Disability("Hearing"))
      }

      val result = old.compareTo(updated)

      assertThat(result)
        .containsExactly(
          "Device wearer's disability or health conditions have changed"
        )
    }

    @Test
    fun `returns message when list is added`() {
      val old = baselineWearer().apply {
        disability = emptyList()
      }
      val updated = baselineWearer()

      val result = old.compareTo(updated)

      assertThat(result)
        .contains("Device wearer's disability or health conditions have changed")
    }

    @Test
    fun `returns message when list is deleted`() {
      val old = baselineWearer()
      val updated = baselineWearer().apply {
        riskCategory = emptyList()
      }

      val result = old.compareTo(updated)

      assertThat(result)
        .contains("Device wearer's risk categories have changed")
    }

    @Test
    fun `returns all relevant messages for multiple changes`() {
      val old = baselineWearer()
      val updated = baselineWearer().apply {
        firstName = "Jane"
        address1 = "456 New Road"
        mappa = "No"
      }

      val result = old.compareTo(updated)

      assertThat(result).containsExactlyInAnyOrder(
        "Device wearer's name has changed",
        "Device wearer's main address has changed",
        "Device wearer's MAPPA has changed"
      )
    }

    @Test
    fun `same message is returned only once even if multiple fields map to it`() {
      val old = baselineWearer()
      val updated = baselineWearer().apply {
        pncId = "PNC123"
        complianceAndEnforcementPersonReference = "CEPR456"
      }

      val result = old.compareTo(updated)

      assertThat(result).containsExactly(
        "Device wearer's personal ID number(s) have changed"
      )
    }

    @Test
    fun `does not return message for unmapped field changes`() {
      val old = baselineWearer()
      val updated = baselineWearer().apply {
        title = "ChangedButUnmapped"
      }

      val result = old.compareTo(updated)

      assertThat(result).isEmpty()
    }



    @ParameterizedTest(name = "changing {0} field emits expected message")
    @ArgumentsSource(DeviceWearerFieldChangeArgumentsProvider::class)
    fun `changing field emits expected message`(
      case: FieldChangeCase
    ) {
      val old = baselineWearer()
      val updated = baselineWearer()
      case.mutate(updated)
      val result = old.compareTo(updated)
      assertThat(result)
        .withFailMessage("Field ${case.name} did not emit expected message")
        .contains(case.expectedMessage)
    }


    @ParameterizedTest(name = "changing {0} does NOT emit any message")
    @ArgumentsSource(DeviceWearerNegativeFieldArgumentsProvider::class)
    fun `changing non mapped field emits no message`(
      case: FieldChangeCase
    ) {
      val old = baselineWearer()
      val updated = baselineWearer()

      case.mutate(updated)

      val result = old.compareTo(updated)

      assertThat(result)
        .withFailMessage(
          "Field ${case.name} should not emit any change message but got: $result"
        )
        .isEmpty()
    }
  }

}

