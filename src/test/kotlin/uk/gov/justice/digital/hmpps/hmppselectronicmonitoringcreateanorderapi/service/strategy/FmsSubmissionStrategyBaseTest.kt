package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult

class TestFmsStrategy(objectMapper: ObjectMapper, activeProfiles: Array<String>, featureFlags: FeatureFlags) :
  FmsSubmissionStrategyBase(objectMapper, activeProfiles, featureFlags) {

  // Expose protected methods as public
  fun executeSerialiseDeviceWearer(deviceWearer: DeviceWearer) = serialiseDeviceWearer(deviceWearer)

  override fun submitOrder(order: Order, orderSource: FmsOrderSource): FmsSubmissionResult {
    TODO("Not yet implemented")
  }
}

class FmsSubmissionStrategyBaseTest {
  @Test
  fun `uses Dev view when active profiles contain 'dev'`() {
    val strategy = TestFmsStrategy(
      ObjectMapper(),
      arrayOf("dev"),
      FeatureFlags(dataDictionaryVersion = DataDictionaryVersion.DDV6, ddV6CourtMappings = true),
    )

    val deviceWearer = DeviceWearer(mappaCaseType = "case type", mappaCategory = "category")
    val result = strategy.executeSerialiseDeviceWearer(deviceWearer)

    assertThat(result.success).isTrue()
    assertThat(result.data).contains("mappaCaseType")
    assertThat(result.data).contains("mappaCategory")
  }

  @Test
  fun `uses prod view when active profiles contain 'prod'`() {
    val strategy = TestFmsStrategy(
      ObjectMapper(),
      arrayOf("prod"),
      FeatureFlags(dataDictionaryVersion = DataDictionaryVersion.DDV6, ddV6CourtMappings = true),
    )

    val deviceWearer = DeviceWearer(mappaCaseType = "case type", mappaCategory = "category")
    val result = strategy.executeSerialiseDeviceWearer(deviceWearer)

    assertThat(result.success).isTrue()
    assertThat(result.data).contains("mappaCaseType")
    assertThat(result.data).doesNotContain("mappaCategory")
  }
}
