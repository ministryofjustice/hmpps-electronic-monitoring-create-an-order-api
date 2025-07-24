package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.scenarios

import com.fasterxml.jackson.databind.ObjectMapper
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import java.nio.file.Files
import java.nio.file.Paths

class ScenarioTests : IntegrationTestBase() {
  @Autowired
  lateinit var fmsResultRepository: FmsSubmissionResultRepository

  @BeforeEach
  fun setup() {
//        repo.deleteAll()
    fmsResultRepository.deleteAll()

    sercoAuthApi.stubGrantToken()
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  fun `It should submit correctly to Serco`(scenarioName: String, fileSource: String) {
    runPayloadTest(fileSource)
  }

  companion object {
    @JvmStatic
    fun scenarios() = listOf(
//      Arguments.of("cemo001", "src/test/resources/json/scenarios/cemo001"),
      Arguments.of("cemo002", "src/test/resources/json/scenarios/cemo002"),
//      Arguments.of("cemo003", "src/test/resources/json/scenarios/cemo003"),
//      Arguments.of("cemo004", "src/test/resources/json/scenarios/cemo004"),
      Arguments.of("cemo005", "src/test/resources/json/scenarios/cemo005"),
//      Arguments.of("cemo006", "src/test/resources/json/scenarios/cemo006"),
      Arguments.of("cemo007", "src/test/resources/json/scenarios/cemo007"),
//      Arguments.of("cemo008", "src/test/resources/json/scenarios/cemo008"),
//      Arguments.of("cemo009", "src/test/resources/json/scenarios/cemo009"),
//      Arguments.of("cemo010", "src/test/resources/json/scenarios/cemo010"),
//      Arguments.of("cemo011", "src/test/resources/json/scenarios/cemo011"),
//      Arguments.of("cemo012", "src/test/resources/json/scenarios/cemo012"),
//      Arguments.of("cemo013", "src/test/resources/json/scenarios/cemo013"),
//      Arguments.of("cemo014", "src/test/resources/json/scenarios/cemo014"),
//      Arguments.of("cemo015", "src/test/resources/json/scenarios/cemo015"),
//      Arguments.of("cemo016", "src/test/resources/json/scenarios/cemo016"),
//      Arguments.of("cemo017", "src/test/resources/json/scenarios/cemo017"),
//      Arguments.of("cemo018", "src/test/resources/json/scenarios/cemo018"),
//      Arguments.of("cemo019", "src/test/resources/json/scenarios/cemo019"),
//      Arguments.of("cemo020", "src/test/resources/json/scenarios/cemo020"),
//      Arguments.of("cemo021", "src/test/resources/json/scenarios/cemo021"),
//      Arguments.of("cemo022", "src/test/resources/json/scenarios/cemo022"),
//      Arguments.of("cemo023", "src/test/resources/json/scenarios/cemo023"),
//      Arguments.of("cemo024", "src/test/resources/json/scenarios/cemo024"),
//      Arguments.of("cemo025", "src/test/resources/json/scenarios/cemo025"),
//      Arguments.of("cemo026", "src/test/resources/json/scenarios/cemo026"),
//      Arguments.of("cemo027", "src/test/resources/json/scenarios/cemo027"),
//      Arguments.of("cemo028", "src/test/resources/json/scenarios/cemo028"),
//      Arguments.of("cemo029", "src/test/resources/json/scenarios/cemo029"),
    )
  }

  fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

  fun runPayloadTest(rootFilePath: String) {
    sercoApi.stubCreateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )
    sercoApi.stubCreateMonitoringOrder(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
    )

    // Create order from JSON and save to DB
    val rawOrder = Files.readString(Paths.get("$rootFilePath/order.json"))
    val objectMapper = ObjectMapper().findAndRegisterModules()
    val order: Order = objectMapper.readValue(rawOrder)

    repo.save(order)

    if (order.additionalDocuments.isNotEmpty()) {
      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.additionalDocuments[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )
      documentApi.stubGetDocument(order.additionalDocuments.first().id.toString())
    }

    if (order.enforcementZoneConditions.isNotEmpty()) {
      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.enforcementZoneConditions[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )
      documentApi.stubGetDocument(order.enforcementZoneConditions[0].fileId.toString())
    }

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

    // Assert
    assertThat(
      submitResult!!.deviceWearerResult.payload,
    ).isEqualTo(expectedDeviceWearerJson.removeWhitespaceAndNewlines())
    assertThat(
      submitResult.monitoringOrderResult.payload,
    ).isEqualTo(expectedOrderJson.removeWhitespaceAndNewlines())
  }
}
