package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
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
  }
}
