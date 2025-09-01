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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as FmsDeviceWearer

@ActiveProfiles("test")
class DeviceWearerTest : OrderTestBase() {

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
