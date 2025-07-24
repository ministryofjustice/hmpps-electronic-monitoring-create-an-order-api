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
      Arguments.of("cemo002", "src/test/resources/json/scenarios/cemo002"),
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
