package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import FmsStateResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRetrieveDWandMO
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import java.util.*

@Service
class FmsClient(@Value("\${services.serco.url}") url: String, private val fmsAuthClient: FmsAuthClient) {

  @Autowired
  lateinit var eventService: EventService
  private val webClient: WebClient = WebClient.builder().baseUrl(url).build()

  private fun resolvePath(orderSource: FmsOrderSource, cemoPath: String, commonPlatformPath: String): String =
    if (orderSource == FmsOrderSource.CEMO) cemoPath else commonPlatformPath

  private fun postFmsRequest(path: String, payload: String, orderId: UUID, errorContext: String): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    return webClient.post().uri(path)
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(payload)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono<FmsErrorResponse>().flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error $errorContext for order: $orderId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
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
      "creating FMS Device Wearer",
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
      errorContext = "updating FMS Monitoring Order",
    )
  }

  fun createAttachment(
    fileName: String,
    caseId: String,
    file: InputStreamResource,
    documentType: String,
    orderRequestType: RequestType,
  ): FmsAttachmentResponse {
    val token = fmsAuthClient.getClientToken()
    var tableName = "x_serg2_ems_csm_sr_mo_new"
    if (orderRequestType === RequestType.VARIATION) {
      tableName = "x_serg2_ems_csm_sr_mo_existing"
    }

    val result = webClient.post()
      .uri { uriBuilder ->
        uriBuilder
          .path("/now/v1/attachment_csm/file")
          .queryParam("table_name", tableName)
          .queryParam("table_sys_id", caseId)
          .queryParam("file_name", fileName)
          .build()
      }
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_OCTET_STREAM) // turns into binary
      .bodyValue(file)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error creating $documentType attachment for order: $caseId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono<FmsAttachmentResponse>()
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun getState(caseId: String): CaseState {
    val token = fmsAuthClient.getClientToken()

    return webClient.get().uri("/now/table/x_serg2_ems_csm_case/$caseId?sysparm_fields=state")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .exchangeToMono { response ->
        when {
          response.statusCode().isError -> {
            Mono.just(CaseState.UNKNOWN)
          }

          else -> {
            response.bodyToMono<FmsStateResponse>()
              .map { res ->
                CaseState.fromStateString(res.result?.state)
              }
          }
        }
      }
      .block()!!
  }

  fun getLastestOrderDetails(caseId: String): FmsRetrieveDWandMO {
    val token = fmsAuthClient.getClientToken()
    return webClient.get()
      .uri("/monitoring_order/retrieveDWandMO?u_case_id=$caseId")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .exchangeToMono { response ->
        when (response.statusCode().value()) {
          400 -> {
            response.bodyToMono<String>()
              .flatMap { error ->
                eventService.recordEvent(
                  "Failed to retrieve latest order from FMS: $caseId",
                  mapOf(
                    "error" to error,
                    "caseId" to caseId,
                  ),
                )
                Mono.error(
                  CreateSercoEntityException(
                    "Invalid request for caseId=$caseId: $error",
                  ),
                )
              }
          }
          404 -> {
            response.bodyToMono<String>()
              .flatMap { error ->
                eventService.recordEvent(
                  "Order details not found from FMS: $caseId",
                  mapOf(
                    "error" to error,
                    "caseId" to caseId,
                  ),
                )
                Mono.error(
                  CreateSercoEntityException(
                    "Case not found for caseId=$caseId: $error",
                  ),
                )
              }
          }
          500 -> {
            response.bodyToMono<String>()
              .defaultIfEmpty("Internal Server Error")
              .flatMap { errorBody ->
                eventService.recordEvent(
                  "Unknow error occurred retrieving latest order from FMS: $caseId",
                  mapOf(
                    "error" to errorBody,
                    "caseId" to caseId,
                  ),
                )
                Mono.error(
                  CreateSercoEntityException(
                    "FMS returned 500 for caseId=$caseId. Response: $errorBody",
                  ),
                )
              }
          }
          else -> {
            response.bodyToMono<FmsRetrieveDWandMO>()
          }
        }
      }
      .block()!!
  }
}
