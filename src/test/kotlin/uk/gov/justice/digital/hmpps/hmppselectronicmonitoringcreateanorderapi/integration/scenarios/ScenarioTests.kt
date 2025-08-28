package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.scenarios

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension.Companion.documentApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import java.nio.file.Files
import java.nio.file.Paths

class ScenarioTests : IntegrationTestBase() {
  @Autowired
  lateinit var fmsResultRepository: FmsSubmissionResultRepository

  private val objectMapper: ObjectMapper = jacksonObjectMapper()

  @BeforeEach
  fun setup() {
    repo.deleteAll()
    fmsResultRepository.deleteAll()

    sercoAuthApi.stubGrantToken()
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  fun `It should submit correctly to Serco`(scenarioName: String, fileSource: String, variation: Boolean = false) {
    runOrderTest(fileSource)
    if (variation) {
      runVariationTest(fileSource)
    }
  }

  companion object {
    @JvmStatic
    fun scenarios() = listOf(
//      Arguments.of("cemo001", "src/test/resources/json/scenarios/cemo001"),
      Arguments.of("cemo002", "src/test/resources/json/scenarios/cemo002", false),
      Arguments.of("cemo003", "src/test/resources/json/scenarios/cemo003", false),
      Arguments.of("cemo004", "src/test/resources/json/scenarios/cemo004", false),
      Arguments.of("cemo005", "src/test/resources/json/scenarios/cemo005", false),
//      Arguments.of("cemo006", "src/test/resources/json/scenarios/cemo006"),
      Arguments.of("cemo007", "src/test/resources/json/scenarios/cemo007", false),
//      Arguments.of("cemo008", "src/test/resources/json/scenarios/cemo008"),
//      Arguments.of("cemo009", "src/test/resources/json/scenarios/cemo009"),
//      Arguments.of("cemo010", "src/test/resources/json/scenarios/cemo010"),
      Arguments.of("cemo011", "src/test/resources/json/scenarios/cemo011", false),
      Arguments.of("cemo012", "src/test/resources/json/scenarios/cemo012", false),
      Arguments.of("cemo013", "src/test/resources/json/scenarios/cemo013", false),
      Arguments.of("cemo014", "src/test/resources/json/scenarios/cemo014", false),
//      Arguments.of("cemo015", "src/test/resources/json/scenarios/cemo015"),
      Arguments.of("cemo016", "src/test/resources/json/scenarios/cemo016", false),
      Arguments.of("cemo017", "src/test/resources/json/scenarios/cemo017", false),
//      Arguments.of("cemo018", "src/test/resources/json/scenarios/cemo018"),
//      Arguments.of("cemo019", "src/test/resources/json/scenarios/cemo019"),
      Arguments.of("cemo020", "src/test/resources/json/scenarios/cemo020", true),
//      Arguments.of("cemo021", "src/test/resources/json/scenarios/cemo021"),
      Arguments.of("cemo022", "src/test/resources/json/scenarios/cemo022", true),
//      Arguments.of("cemo023", "src/test/resources/json/scenarios/cemo023"),
      Arguments.of("cemo024", "src/test/resources/json/scenarios/cemo024", true),
//      Arguments.of("cemo034", "src/test/resources/json/scenarios/cemo034", true),
//      Arguments.of("cemo035", "src/test/resources/json/scenarios/cemo035", true),
      Arguments.of("cemo036", "src/test/resources/json/scenarios/cemo036", false),
    )
  }

  fun runOrderTest(rootFilePath: String) {
    sercoApi.stubCreateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )
    sercoApi.stubCreateMonitoringOrder(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
    )

    val order = getOrderFromFile("$rootFilePath/order.json")

    repo.save(order)

    stubAttachments(order)

    // Submit order
    webTestClient.post()
      .uri("/api/orders/${order.id}/submit")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    val submitResult = fmsResultRepository.findAll().first()
    assertThat(submitResult).isNotNull

    // Read expected JSON
    val expectedDeviceWearerJson = Files.readString(Paths.get("$rootFilePath/expected_device_wearer.json"))
    val expectedOrderJson = Files.readString(Paths.get("$rootFilePath/expected_order.json"))
      .replace("{expectedOderId}", submitResult.orderId.toString())

    val expectedDeviceWearer = objectMapper.readValue<DeviceWearer>(expectedDeviceWearerJson)

    val expectedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(expectedOrderJson)

    // Assert

    val storedDeviceWearer = objectMapper.readValue<DeviceWearer>(submitResult.deviceWearerResult.payload)
    assertThat(storedDeviceWearer).isEqualTo(expectedDeviceWearer)

    val storedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(submitResult.monitoringOrderResult.payload)
    assertThat(storedMonitoringOrder).isEqualTo(expectedMonitoringOrder)
  }

  fun runVariationTest(rootFilePath: String) {
    sercoApi.stubUpdateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )
    sercoApi.stubUpdateMonitoringOrder(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
    )

    val variation: Order = getOrderFromFile("$rootFilePath/variation.json")

    repo.save(variation)

    stubAttachments(variation, "x_serg2_ems_csm_sr_mo_existing")

    // Submit order
    webTestClient.post()
      .uri("/api/orders/${variation.id}/submit")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    val submitResult = fmsResultRepository.findAll().first()
    assertThat(submitResult).isNotNull

    // Read expected JSON
    val expectedDeviceWearerJson = Files.readString(Paths.get("$rootFilePath/expected_updated_device_wearer.json"))
    val expectedVariationJson = Files.readString(Paths.get("$rootFilePath/expected_variation.json"))
      .replace("{expectedOderId}", submitResult.orderId.toString())

    val expectedDeviceWearer = objectMapper.readValue<DeviceWearer>(expectedDeviceWearerJson)

    val expectedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(expectedVariationJson)

    // Assert
    val storedDeviceWearer = objectMapper.readValue<DeviceWearer>(submitResult.deviceWearerResult.payload)
    assertThat(expectedDeviceWearer).isEqualTo(storedDeviceWearer)

    val storedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(submitResult.monitoringOrderResult.payload)
    assertThat(expectedMonitoringOrder).isEqualTo(storedMonitoringOrder)
  }

  private fun getOrderFromFile(filePath: String): Order {
    // Create variation from JSON and save to DB
    val rawOrder = Files.readString(Paths.get(filePath))
    val objectMapper = ObjectMapper().findAndRegisterModules()
    return objectMapper.readValue(rawOrder)
  }

  private fun stubAttachments(order: Order, tableName: String = "x_serg2_ems_csm_sr_mo_new") {
    if (order.additionalDocuments.isNotEmpty()) {
      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.additionalDocuments[0].fileName!!,
            tableName = tableName,
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )
      documentApi.stubGetDocument(order.additionalDocuments.first().documentId.toString())

      if (order.additionalDocuments.size == 2) {
        sercoApi.stubSubmitAttachment(
          HttpStatus.OK,
          FmsAttachmentResponse(
            result = FmsAttachmentResult(
              fileName = order.additionalDocuments[1].fileName!!,
              tableName = tableName,
              sysId = "MockSysId",
              tableSysId = "MockDeviceWearerId",
            ),
          ),
        )
        documentApi.stubGetDocument(order.additionalDocuments[1].documentId.toString())
      }
    }

    if (order.enforcementZoneConditions.isNotEmpty()) {
      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.enforcementZoneConditions[0].fileName!!,
            tableName = tableName,
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )
      documentApi.stubGetDocument(order.enforcementZoneConditions[0].fileId.toString())
    }
  }
}
