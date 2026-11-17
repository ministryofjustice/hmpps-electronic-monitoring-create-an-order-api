package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import FmsStateResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.codec.DecodingException
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.CorrelationIdContext
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.FmsCallException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.FmsFailureCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRetrieveDWandMO
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import java.util.*
import java.util.concurrent.TimeoutException

@Service
class FmsClient(
  @Value("\${services.serco.url}") url: String,
  private val fmsAuthClient: FmsAuthClient,
  private val eventService: EventService,
) {

  private val webClient: WebClient = WebClient.builder().baseUrl(url).build()
  private val logger = LoggerFactory.getLogger(FmsClient::class.java)

  private fun <S : WebClient.RequestHeadersSpec<S>> addCorrelationId(request: S): S {
    CorrelationIdContext.current()?.let { request.header(CorrelationIdContext.HEADER_NAME, it) }
    return request
  }

  private fun <T> withFmsTelemetry(operation: String, fallback: (() -> T)? = null, request: () -> T): T {
    val startTime = System.nanoTime()
    return try {
      val result = request()
      val durationMs = (System.nanoTime() - startTime) / 1_000_000
      recordFmsEvent(operation, "success", null, null, durationMs)
      logger.debug("FMS call completed operation={} durationMs={}", operation, durationMs)
      result
    } catch (exception: Exception) {
      val failure = classifyFailure(operation, exception)
      val durationMs = (System.nanoTime() - startTime) / 1_000_000
      recordFmsEvent(
        operation,
        "failure",
        failure.failureCategory,
        failure.httpStatusCode,
        durationMs,
      )
      logger.warn(
        "FMS call failed operation={} category={} httpStatusCode={} durationMs={}",
        operation,
        failure.failureCategory.telemetryValue,
        failure.httpStatusCode,
        durationMs,
      )
      if (fallback != null) fallback() else throw failure
    }
  }

  private fun recordFmsEvent(
    operation: String,
    outcome: String,
    failureCategory: FmsFailureCategory?,
    httpStatusCode: Int?,
    durationMs: Long,
  ) {
    val properties = buildMap {
      put("operation", operation)
      put("outcome", outcome)
      failureCategory?.let { put("failureCategory", it.telemetryValue) }
      httpStatusCode?.let { put("httpStatusCode", it.toString()) }
    }
    runCatching { eventService.recordEvent("FMS_Dependency_Call", properties, durationMs) }
  }

  private fun classifyFailure(operation: String, exception: Exception): FmsCallException {
    if (exception is FmsCallException) return exception

    val statusCode = (exception as? WebClientResponseException)?.statusCode?.value()
    val hasTimeoutCause = generateSequence(exception as Throwable?) { it.cause }.any {
      it is TimeoutException || it.javaClass.simpleName.contains("Timeout", ignoreCase = true)
    }
    val category = when {
      statusCode != null -> FmsFailureCategory.HTTP_ERROR
      hasTimeoutCause -> FmsFailureCategory.TIMEOUT
      exception is WebClientRequestException -> FmsFailureCategory.TRANSPORT_ERROR
      exception is DecodingException -> FmsFailureCategory.RESPONSE_ERROR
      else -> FmsFailureCategory.CLIENT_ERROR
    }
    val statusText = statusCode?.let { ", HTTP $it" } ?: ""
    return FmsCallException(
      operation = operation,
      failureCategory = category,
      httpStatusCode = statusCode,
      message = "FMS $operation failed (${category.telemetryValue}$statusText)",
      cause = exception,
    )
  }

  private fun fmsHttpError(
    response: ClientResponse,
    operation: String,
    message: (String?, Int) -> String,
  ): Mono<Throwable> {
    val statusCode = response.statusCode().value()
    return response.bodyToMono<FmsErrorResponse>()
      .map<Throwable> { error ->
        FmsCallException(
          operation,
          FmsFailureCategory.HTTP_ERROR,
          statusCode,
          message(error.error?.detail, statusCode),
        )
      }
      .defaultIfEmpty(
        FmsCallException(
          operation,
          FmsFailureCategory.HTTP_ERROR,
          statusCode,
          message(null, statusCode),
        ),
      )
      .onErrorResume { cause ->
        Mono.just(
          FmsCallException(
            operation,
            FmsFailureCategory.RESPONSE_ERROR,
            statusCode,
            "FMS $operation returned HTTP $statusCode with an unreadable error response",
            cause,
          ),
        )
      }
  }

  private fun resolvePath(orderSource: FmsOrderSource, cemoPath: String, commonPlatformPath: String): String =
    if (orderSource == FmsOrderSource.CEMO) cemoPath else commonPlatformPath

  private fun postFmsRequest(
    path: String,
    payload: String,
    orderId: UUID,
    operation: String,
    errorContext: String,
  ): FmsResponse = withFmsTelemetry(operation) {
    val token = fmsAuthClient.getClientToken()
    addCorrelationId(webClient.post().uri(path))
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(payload)
      .retrieve()
      .onStatus({ status -> status.isError }) { response ->
        fmsHttpError(response, operation) { detail, statusCode ->
          "Error $errorContext for order: $orderId with error: ${detail ?: "HTTP $statusCode"}"
        }
      }
      .bodyToMono(FmsResponse::class.java)
      .block() ?: throw FmsCallException(
      operation,
      FmsFailureCategory.RESPONSE_ERROR,
      message = "FMS $operation returned an empty response",
    )
  }

  fun createDeviceWearer(deviceWearerPayload: String, orderId: UUID, orderSource: FmsOrderSource): FmsResponse {
    val path = resolvePath(
      orderSource,
      cemoPath = "/x_seem_cemo/device_wearer/createDW",
      commonPlatformPath = "/x_seem_cemo/device_wearer/createCPDW",
    )

    return postFmsRequest(
      path,
      deviceWearerPayload,
      orderId,
      operation = "create_device_wearer",
      errorContext = "creating FMS Device Wearer",
    )
  }

  fun createMonitoringOrder(orderPayload: String, orderId: UUID, orderSource: FmsOrderSource): FmsResponse {
    val path = resolvePath(
      orderSource,
      cemoPath = "/x_seem_cemo/monitoring_order/createMO",
      commonPlatformPath = "/x_seem_cemo/monitoring_order/createCPMO",
    )

    return postFmsRequest(
      path,
      orderPayload,
      orderId,
      operation = "create_monitoring_order",
      errorContext = "creating FMS Monitoring Order",
    )
  }

  fun updateDeviceWearer(deviceWearerPayload: String, orderId: UUID, orderSource: FmsOrderSource): FmsResponse {
    val path = resolvePath(
      orderSource,
      cemoPath = "/x_seem_cemo/device_wearer/updateDW",
      commonPlatformPath = "/x_seem_cemo/device_wearer/updateCPDW",
    )

    return postFmsRequest(
      path,
      deviceWearerPayload,
      orderId,
      operation = "update_device_wearer",
      errorContext = "updating FMS Device Wearer",
    )
  }

  fun updateMonitoringOrder(orderPayload: String, orderId: UUID, orderSource: FmsOrderSource): FmsResponse {
    val path = resolvePath(
      orderSource,
      cemoPath = "/x_seem_cemo/monitoring_order/updateMO",
      commonPlatformPath = "/x_seem_cemo/monitoring_order/updateCPMO",
    )

    return postFmsRequest(
      path,
      orderPayload,
      orderId,
      operation = "update_monitoring_order",
      errorContext = "updating FMS Monitoring Order",
    )
  }

  fun createAttachment(
    fileName: String,
    caseId: String,
    file: InputStreamResource,
    documentType: String,
    orderRequestType: RequestType,
  ): FmsAttachmentResponse = withFmsTelemetry("create_attachment") {
    val token = fmsAuthClient.getClientToken()
    var tableName = "x_serg2_ems_csm_sr_mo_new"
    if (orderRequestType === RequestType.VARIATION) {
      tableName = "x_serg2_ems_csm_sr_mo_existing"
    }

    addCorrelationId(
      webClient.post().uri { uriBuilder ->
        uriBuilder
          .path("/now/v1/attachment_csm/file")
          .queryParam("table_name", tableName)
          .queryParam("table_sys_id", caseId)
          .queryParam("file_name", fileName)
          .build()
      },
    )
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_OCTET_STREAM) // turns into binary
      .bodyValue(file)
      .retrieve()
      .onStatus({ status -> status.isError }) { response ->
        fmsHttpError(response, "create_attachment") { detail, statusCode ->
          "Error creating $documentType attachment for order: $caseId with error: ${detail ?: "HTTP $statusCode"}"
        }
      }
      .bodyToMono<FmsAttachmentResponse>()
      .block() ?: throw FmsCallException(
      "create_attachment",
      FmsFailureCategory.RESPONSE_ERROR,
      message = "FMS create_attachment returned an empty response",
    )
  }

  fun getState(caseId: String): CaseState = withFmsTelemetry(
    operation = "get_case_state",
    fallback = { CaseState.UNKNOWN },
  ) {
    val token = fmsAuthClient.getClientToken()

    addCorrelationId(webClient.get().uri("/now/table/x_serg2_ems_csm_case/$caseId?sysparm_fields=state"))
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .exchangeToMono { response ->
        when {
          response.statusCode().isError -> {
            Mono.error(
              FmsCallException(
                "get_case_state",
                FmsFailureCategory.HTTP_ERROR,
                response.statusCode().value(),
                "FMS get_case_state returned HTTP ${response.statusCode().value()}",
              ),
            )
          }

          else -> {
            response.bodyToMono<FmsStateResponse>()
              .map { res ->
                CaseState.fromStateString(res.result?.state)
              }
          }
        }
      }
      .block() ?: throw FmsCallException(
      "get_case_state",
      FmsFailureCategory.RESPONSE_ERROR,
      message = "FMS get_case_state returned an empty response",
    )
  }

  fun getLatestOrderDetails(caseId: String): FmsRetrieveDWandMO = withFmsTelemetry("retrieve_order_details") {
    val token = fmsAuthClient.getClientToken()
    addCorrelationId(webClient.get().uri("/monitoring_order/retrieveDWandMO?u_case_id=$caseId"))
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .exchangeToMono { response ->
        if (response.statusCode().isError) {
          val statusCode = response.statusCode().value()
          response.bodyToMono<String>()
            .defaultIfEmpty("")
            .flatMap { errorBody ->
              Mono.error(
                FmsCallException(
                  "retrieve_order_details",
                  FmsFailureCategory.HTTP_ERROR,
                  statusCode,
                  "FMS returned $statusCode for caseId=$caseId: $errorBody",
                ),
              )
            }
        } else {
          response.bodyToMono<FmsRetrieveDWandMO>()
        }
      }
      .block() ?: throw FmsCallException(
      "retrieve_order_details",
      FmsFailureCategory.RESPONSE_ERROR,
      message = "FMS retrieve_order_details returned an empty response",
    )
  }
}
