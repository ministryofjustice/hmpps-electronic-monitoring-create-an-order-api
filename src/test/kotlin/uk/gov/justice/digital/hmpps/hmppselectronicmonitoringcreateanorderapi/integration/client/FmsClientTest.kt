package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.client

import FmsState
import FmsStateResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.internal.verification.Times
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRetrieveDWandMO
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import java.io.ByteArrayInputStream
import java.util.*

@ActiveProfiles("test")
class FmsClientTest : IntegrationTestBase() {

  @Autowired
  lateinit var fmsClient: FmsClient

  @Autowired
  lateinit var objectMapper: ObjectMapper

  lateinit var eventService: EventService

  @BeforeEach
  fun setup() {
    eventService = Mockito.mock(EventService::class.java)
    fmsClient.eventService = eventService
  }

  @Nested
  @DisplayName("POST /api/x_seem_cemo/device_wearer/createDW")
  inner class CreateDeviceWearer {
    @Test
    fun `it should handle 403 responses`() {
      // Given
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubCreateDeviceWearer(
        status = HttpStatus.UNAUTHORIZED,
        result = FmsResponse(),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "User not authorised", detail = "User is unauthorised"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.createDeviceWearer(mockDeviceWearerPayload(), orderId, FmsOrderSource.CEMO)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error creating FMS Device Wearer for order: $orderId with error: User is unauthorised")
    }

    @Test
    fun `it should handle 500 responses`() {
      // Given
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubCreateDeviceWearer(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        result = FmsResponse(),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "An error message", detail = "Error detail"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.createDeviceWearer(mockDeviceWearerPayload(), orderId, FmsOrderSource.CEMO)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error creating FMS Device Wearer for order: $orderId with error: Error detail")
    }

    @Test
    fun `it should call the common platform endpoint when orderSource is COMMON_PLATFORM`() {
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubCreateCommonPlatformDeviceWearer(
        status = HttpStatus.OK,
        result = FmsResponse(result = listOf(FmsResult(message = "mock response", id = "1")), status = "200"),
      )

      val result = fmsClient.createDeviceWearer(mockDeviceWearerPayload(), orderId, FmsOrderSource.COMMON_PLATFORM)

      assertThat(result.result.first().id).isEqualTo("1")
    }
  }

  @Nested
  @DisplayName("POST /api/x_seem_cemo/monitoring_order/createMO")
  inner class CreateMonitoringOrder {
    @Test
    fun `it should handle 403 responses`() {
      // Given
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubCreateMonitoringOrder(
        status = HttpStatus.UNAUTHORIZED,
        result = FmsResponse(),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "User not authorised", detail = "User is unauthorised"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.createMonitoringOrder(mockMonitoringOrderPayload(), orderId, FmsOrderSource.CEMO)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error creating FMS Monitoring Order for order: $orderId with error: User is unauthorised")
    }

    @Test
    fun `it should handle 500 responses`() {
      // Given
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubCreateMonitoringOrder(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        result = FmsResponse(),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "An error message", detail = "Error detail"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.createMonitoringOrder(mockMonitoringOrderPayload(), orderId, FmsOrderSource.CEMO)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error creating FMS Monitoring Order for order: $orderId with error: Error detail")
    }

    @Test
    fun `it should call the common platform endpoint when orderSource is COMMON_PLATFORM`() {
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubCreateCommonPlatformMonitoringOrder(
        status = HttpStatus.OK,
        result = FmsResponse(result = listOf(FmsResult(message = "mock response", id = "1")), status = "200"),
      )

      val result = fmsClient.createMonitoringOrder(
        mockMonitoringOrderPayload(),
        orderId,
        FmsOrderSource.COMMON_PLATFORM,
      )

      assertThat(result.result.first().id).isEqualTo("1")
    }
  }

  @Nested
  @DisplayName("POST /api/x_seem_cemo/device_wearer/updateDW")
  inner class UpdateDeviceWearer {
    @Test
    fun `it should handle 403 responses`() {
      // Given
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubUpdateDeviceWearer(
        status = HttpStatus.UNAUTHORIZED,
        result = FmsResponse(),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "User not authorised", detail = "User is unauthorised"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.updateDeviceWearer(mockDeviceWearerPayload(), orderId, FmsOrderSource.CEMO)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error updating FMS Device Wearer for order: $orderId with error: User is unauthorised")
    }

    @Test
    fun `it should handle 500 responses`() {
      // Given
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubUpdateDeviceWearer(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        result = FmsResponse(),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "An error message", detail = "Error detail"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.updateDeviceWearer(mockDeviceWearerPayload(), orderId, FmsOrderSource.CEMO)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error updating FMS Device Wearer for order: $orderId with error: Error detail")
    }

    @Test
    fun `it should call the common platform endpoint when orderSource is COMMON_PLATFORM`() {
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubUpdateCommonPlatformDeviceWearer(
        status = HttpStatus.OK,
        result = FmsResponse(result = listOf(FmsResult(message = "mock response", id = "1")), status = "200"),
      )

      val result = fmsClient.updateDeviceWearer(mockDeviceWearerPayload(), orderId, FmsOrderSource.COMMON_PLATFORM)

      assertThat(result.result.first().id).isEqualTo("1")
    }
  }

  @Nested
  @DisplayName("POST /api/x_seem_cemo/monitoring_order/updateMO")
  inner class UpdateMonitoringOrder {
    @Test
    fun `it should handle 403 responses`() {
      // Given
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubUpdateMonitoringOrder(
        status = HttpStatus.UNAUTHORIZED,
        result = FmsResponse(),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "User not authorised", detail = "User is unauthorised"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.updateMonitoringOrder(mockMonitoringOrderPayload(), orderId, FmsOrderSource.CEMO)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error updating FMS Monitoring Order for order: $orderId with error: User is unauthorised")
    }

    @Test
    fun `it should handle 500 responses`() {
      // Given
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubUpdateMonitoringOrder(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        result = FmsResponse(),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "An error message", detail = "Error detail"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.updateMonitoringOrder(mockMonitoringOrderPayload(), orderId, FmsOrderSource.CEMO)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error updating FMS Monitoring Order for order: $orderId with error: Error detail")
    }

    @Test
    fun `it should call the common platform endpoint when orderSource is COMMON_PLATFORM`() {
      val orderId = UUID.randomUUID()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubUpdateCommonPlatformMonitoringOrder(
        status = HttpStatus.OK,
        result = FmsResponse(result = listOf(FmsResult(message = "mock response", id = "1")), status = "200"),
      )

      val result = fmsClient.updateMonitoringOrder(
        mockMonitoringOrderPayload(),
        orderId,
        FmsOrderSource.COMMON_PLATFORM,
      )

      assertThat(result.result.first().id).isEqualTo("1")
    }
  }

  @Nested
  @DisplayName("POST /api/now/v1/attachment_csm/file")
  inner class CreateAttachment {
    @Test
    fun `it should handle 403 responses`() {
      // Given
      val documentType = "image"
      val caseId = "123456789"
      val fileName = "profile.jpeg"
      sercoAuthApi.stubGrantToken()
      sercoApi.stubSubmitAttachment(
        status = HttpStatus.UNAUTHORIZED,
        result = FmsAttachmentResponse(
          FmsAttachmentResult(
            tableName = "x_serg2_ems_csm_sr_mo_new",
            tableSysId = caseId,
            fileName = fileName,
          ),
        ),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "User not authorised", detail = "User is unauthorised"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.createAttachment(
          fileName = fileName,
          caseId = caseId,
          file = InputStreamResource(
            ByteArrayInputStream("".toByteArray()),
          ),
          documentType = documentType,
          orderRequestType = RequestType.REQUEST,
        )
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error creating $documentType attachment for order: $caseId with error: User is unauthorised")
    }

    @Test
    fun `it should handle 500 responses`() {
      // Given
      val documentType = "image"
      val caseId = "123456789"
      val fileName = "profile.jpeg"
      sercoAuthApi.stubGrantToken()
      sercoApi.stubSubmitAttachment(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        result = FmsAttachmentResponse(
          FmsAttachmentResult(
            tableName = "x_serg2_ems_csm_sr_mo_new",
            tableSysId = caseId,
            fileName = fileName,
          ),
        ),
        errorResponse = FmsErrorResponse(
          error = ErrorResponse(message = "An error message", detail = "Error detail"),
        ),
      )

      // When
      val exception = assertThrows<CreateSercoEntityException> {
        fmsClient.createAttachment(
          fileName = fileName,
          caseId = caseId,
          file = InputStreamResource(
            ByteArrayInputStream("".toByteArray()),
          ),
          documentType = documentType,
          orderRequestType = RequestType.REQUEST,
        )
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error creating $documentType attachment for order: $caseId with error: Error detail")
    }
  }

  @Nested
  @DisplayName("Get api/now/table/x_serg2_ems_csm_case/{case_id}")
  inner class GetState {
    @Test
    fun `it should return state of Unknown for 404 status`() {
      val caseId = "mockCaseId"
      sercoAuthApi.stubGrantToken()
      sercoApi.stubGetState(
        caseId,
        status = HttpStatus.NOT_FOUND,
        result = FmsStateResponse(result = null),
      )

      val result = fmsClient.getState(caseId)

      assertThat(
        result,
      ).isEqualTo(CaseState.UNKNOWN)
    }

    @Test
    fun `should return successful result`() {
      val caseId = "mockCaseId"
      sercoAuthApi.stubGrantToken()
      sercoApi.stubGetState(
        caseId,
        status = HttpStatus.OK,
        result = FmsStateResponse(FmsState("1")),
      )

      val result = fmsClient.getState(caseId)

      assertThat(
        result,
      ).isEqualTo(CaseState.NEW)
    }
  }

  @Nested
  @DisplayName("GET /monitoring_order/retrieveDWandMO?u_case_id")
  inner class RetrieveDWandMO {
    @Test
    fun `should return order details when response is successful`() {
      val expected = FmsRetrieveDWandMO(
        caseId = "CASE123",
        deviceWearer = DeviceWearer(),
        monitoringOrder = MonitoringOrder(),
      )

      sercoAuthApi.stubGrantToken()
      sercoApi.stubGetDWandMo(
        "CASE123",
        status = HttpStatus.OK,
        result = expected,
      )

      val result = fmsClient.getLastestOrderDetails("CASE123")
      assertThat(result).isEqualTo(expected)
      verify(eventService, Times(0)).recordEvent(any(), any(), any())
    }

    @Test
    fun `should throw exception and record event for 400 response`() {
      val caseId = "CASE123"
      val expected = FmsRetrieveDWandMO(
        caseId = caseId,
        deviceWearer = DeviceWearer(),
        monitoringOrder = MonitoringOrder(),
      )
      val errorResponse = """
          {
            "result": {
              "error": "u_case_id query parameter is required"
            }
          }
      """.trimIndent()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubGetDWandMo(
        caseId,
        status = HttpStatus.BAD_REQUEST,
        result = expected,
        errorResponse = errorResponse,
      )


      assertThatThrownBy {
        fmsClient.getLastestOrderDetails(caseId)
      }
        .isInstanceOf(CreateSercoEntityException::class.java)
        .hasMessageContaining("Invalid request")


      verify(eventService).recordEvent(
        eq("Failed to retrieve latest order from FMS: $caseId"),
        eq(
          mapOf(
            "error" to errorResponse,
            "caseId" to caseId,
          ),
        ),
        any()
      )
    }

    @Test
    fun `should throw exception and record event for 404 response`() {
      val caseId = "CASE123"
      val expected = FmsRetrieveDWandMO(
        caseId = caseId,
        deviceWearer = DeviceWearer(),
        monitoringOrder = MonitoringOrder(),
      )
      val errorResponse = """
          {
          "error": {        
            "message": "Service Error",        
            "detail": "Unable to locate this case in ServiceNow"        
          },        
          "status": "failure"        
        }
      """.trimIndent()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubGetDWandMo(
        caseId,
        status = HttpStatus.NOT_FOUND,
        result = expected,
        errorResponse = errorResponse,
      )


      assertThatThrownBy {
        fmsClient.getLastestOrderDetails(caseId)
      }
        .isInstanceOf(CreateSercoEntityException::class.java)
        .hasMessageContaining("Case not found")


      verify(eventService).recordEvent(
        eq("Order details not found from FMS: $caseId"),
        eq(
          mapOf(
            "error" to errorResponse,
            "caseId" to caseId,
          ),
        ),
        any()
      )
    }

    @Test
    fun `should throw exception and record event for 500 response`() {
      val caseId = "CASE123"
      val expected = FmsRetrieveDWandMO(
        caseId = caseId,
        deviceWearer = DeviceWearer(),
        monitoringOrder = MonitoringOrder(),
      )
      val errorResponse = """
          {
          "error": {        
            "message": "Service Error",        
            "detail": "Unkown error"        
          },        
          "status": "failure"        
        }
      """.trimIndent()
      sercoAuthApi.stubGrantToken()
      sercoApi.stubGetDWandMo(
        caseId,
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        result = expected,
        errorResponse = errorResponse,
      )


      assertThatThrownBy {
        fmsClient.getLastestOrderDetails(caseId)
      }
        .isInstanceOf(CreateSercoEntityException::class.java)
        .hasMessageContaining("FMS returned 500")


      verify(eventService).recordEvent(
        eq("Unknow error occurred retrieving latest order from FMS: $caseId"),
        eq(
          mapOf(
            "error" to errorResponse,
            "caseId" to caseId,
          ),
        ),
        any()
      )
    }
  }

  fun mockDeviceWearerPayload(): String = objectMapper.writeValueAsString(DeviceWearer())

  fun mockMonitoringOrderPayload(): String = objectMapper.writeValueAsString(MonitoringOrder())
}
