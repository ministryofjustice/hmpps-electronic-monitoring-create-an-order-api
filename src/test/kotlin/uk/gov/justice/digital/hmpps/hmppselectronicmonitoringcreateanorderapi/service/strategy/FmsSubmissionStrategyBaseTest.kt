package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.strategy

import DeviceWearerPayloadVersion
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import java.text.SimpleDateFormat

class TestFmsStrategy(objectMapper: ObjectMapper, featureFlags: FeatureFlags) :
  FmsSubmissionStrategyBase(objectMapper, featureFlags) {

  fun executeSerialiseDeviceWearer(deviceWearer: DeviceWearer) = serialiseDeviceWearer(deviceWearer)

  override fun submitOrder(order: Order, orderSource: FmsOrderSource): FmsSubmissionResult {
    TODO("Not yet implemented")
  }
}

class FmsSubmissionStrategyBaseTest {
  @Test
  fun `uses Dev view when deviceWearerPayloadVersion is Dev`() {
    val objectMapper = JsonMapper.builder()
      .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
      .defaultDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
      .build()

    val strategy = TestFmsStrategy(
      objectMapper,
      FeatureFlags(
        dataDictionaryVersion = DataDictionaryVersion.DDV6,
        ddV6CourtMappings = true,
        deviceWearerPayloadVersion = DeviceWearerPayloadVersion.Dev,
      ),
    )

    val deviceWearer = DeviceWearer(mappaCaseType = "case type", mappaCategory = "category")
    val result = strategy.executeSerialiseDeviceWearer(deviceWearer)

    assertThat(result.success).isTrue()
    assertThat(result.data).contains("mappa_case_type")
    assertThat(result.data).contains("mappa_category")
  }

//  @Test
//  fun `uses Dev view when deviceWearerPayloadVersion is Prod`() {
//    val strategy = TestFmsStrategy(
//      ObjectMapper(),
//      FeatureFlags(
//        dataDictionaryVersion = DataDictionaryVersion.DDV6,
//        ddV6CourtMappings = true,
//        deviceWearerPayloadVersion = DeviceWearerPayloadVersion.Prod,
//      ),
//    )
//
//    val deviceWearer = DeviceWearer(mappaCaseType = "case type", mappaCategory = "category")
//    val result = strategy.executeSerialiseDeviceWearer(deviceWearer)
//
//    assertThat(result.success).isTrue()
//    assertThat(result.data).contains("mappaCaseType")
//    assertThat(result.data).doesNotContain("mappaCategory")
//  }
}
