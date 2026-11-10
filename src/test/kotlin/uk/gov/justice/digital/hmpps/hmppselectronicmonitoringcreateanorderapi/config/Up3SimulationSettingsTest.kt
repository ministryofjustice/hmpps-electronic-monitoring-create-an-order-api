package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mock.env.MockEnvironment

class Up3SimulationSettingsTest {
  @ParameterizedTest
  @ValueSource(strings = ["dev", "local"])
  fun `permits enabled simulation without another credential`(profile: String) {
    val env = MockEnvironment().withProperty("settings.return-listener-enabled", "true")
    env.setActiveProfiles(profile)
    val settings = Up3SimulationSettings(true, env)
    assertThat(settings.enabled).isTrue()
    assertThat(settings.storageAvailable).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["prod", "preprod", "test", ""])
  fun `rejects enabled simulation elsewhere and does not enable its storage`(profile: String) {
    val env = MockEnvironment().withProperty("settings.return-listener-enabled", "true")
    if (profile.isNotEmpty()) env.setActiveProfiles(profile)
    assertThat(Up3SimulationSettings(false, env).storageAvailable).isFalse()
    assertThatThrownBy { Up3SimulationSettings(true, env) }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `rejects mixed production and dev profiles even with controls disabled`() {
    val env = MockEnvironment()
    env.setActiveProfiles("dev", "prod")
    assertThatThrownBy { Up3SimulationSettings(false, env) }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `requires an enabled listener`() {
    val env = MockEnvironment()
    env.setActiveProfiles("dev")
    assertThatThrownBy { Up3SimulationSettings(true, env) }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `turning off dev controls retains storage for routing protection`() {
    val env = MockEnvironment()
    env.setActiveProfiles("dev", "test")
    val settings = Up3SimulationSettings(false, env)
    assertThat(settings.enabled).isFalse()
    assertThat(settings.storageAvailable).isTrue()
  }
}
