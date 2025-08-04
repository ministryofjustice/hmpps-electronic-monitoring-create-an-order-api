package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import java.io.ByteArrayInputStream
import java.util.*

@ActiveProfiles("test")
class FmsClientTest : IntegrationTestBase() {

  @Autowired
  lateinit var fmsClient: FmsClient

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
        fmsClient.createDeviceWearer(DeviceWearer(), orderId)
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
        fmsClient.createDeviceWearer(DeviceWearer(), orderId)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error creating FMS Device Wearer for order: $orderId with error: Error detail")
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
        fmsClient.createMonitoringOrder(MonitoringOrder(), orderId)
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
        fmsClient.createMonitoringOrder(MonitoringOrder(), orderId)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error creating FMS Monitoring Order for order: $orderId with error: Error detail")
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
        fmsClient.updateDeviceWearer(DeviceWearer(), orderId)
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
        fmsClient.updateDeviceWearer(DeviceWearer(), orderId)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error updating FMS Device Wearer for order: $orderId with error: Error detail")
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
        fmsClient.updateMonitoringOrder(MonitoringOrder(), orderId)
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
        fmsClient.updateMonitoringOrder(MonitoringOrder(), orderId)
      }

      // Then
      assertThat(
        exception.message,
      ).isEqualTo("Error updating FMS Monitoring Order for order: $orderId with error: Error detail")
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
}
