package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.OrderTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.enums.ddv4.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.enums.ddv4.Gender
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.enums.ddv4.RiskCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.enums.ddv4.Sex
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
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
  @EnumSource(Sex::class)
  fun `It should correctly map saved sex values to Serco`(sex: Sex) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(sex = sex.name),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.sex).isEqualTo(sex.value)
  }

  @ParameterizedTest(name = "it should map saved gender values to Serco - {0} -> {1}")
  @EnumSource(Gender::class)
  fun `It should correctly map saved gender values to Serco`(gender: Gender) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(gender = gender.name),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.genderIdentity).isEqualTo(gender.value)
  }

  @ParameterizedTest(name = "it should map saved risk category values to Serco - {0} -> {1}")
  @EnumSource(
    value = RiskCategory::class,
    names = ["NO_RISK"],
    mode = EnumSource.Mode.EXCLUDE,
  )
  fun `It should correctly map saved risk category values to Serco`(riskCategory: RiskCategory) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(),
      installationAndRisk = createInstallationAndRisk(riskCategory = riskCategory.name),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.riskCategory!!.first().category).isEqualTo(riskCategory.value)
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
  @EnumSource(Disability::class)
  fun `It should correctly map saved disability values to Serco`(disability: Disability) {
    val order = createOrder(
      deviceWearer = createDeviceWearer(disabilities = disability.name),
    )
    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)

    assertThat(fmsDeviceWearer.disability!!.first().disability).isEqualTo(disability.value)
  }
}
