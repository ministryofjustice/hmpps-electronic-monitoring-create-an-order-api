package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import FmsStateResponse
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SercoConnectionException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import java.util.*

@Service
class FmsClient(@Value("\${services.serco.url}") url: String, private val fmsAuthClient: FmsAuthClient) {
  private val webClient: WebClient = WebClient.builder().baseUrl(url).build()

  fun createDeviceWearer(deviceWearerPayload: String, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/x_seem_cemo/device_wearer/createDW")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(deviceWearerPayload)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error creating FMS Device Wearer for order: $orderId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun createMonitoringOrder(orderPayload: String, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/x_seem_cemo/monitoring_order/createMO")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(orderPayload)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error creating FMS Monitoring Order for order: $orderId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun updateDeviceWearer(deviceWearerPayload: String, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/x_seem_cemo/device_wearer/updateDW")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(deviceWearerPayload)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error updating FMS Device Wearer for order: $orderId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun updateMonitoringOrder(orderPayload: String, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/x_seem_cemo/monitoring_order/updateMO")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(orderPayload)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error updating FMS Monitoring Order for order: $orderId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
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
      .bodyToMono(FmsAttachmentResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun getState(caseId: String): FmsStateResponse {
    val token = fmsAuthClient.getClientToken()

    val result = webClient.get().uri("/now/table/x_serg2_ems_csm_case/$caseId?sysparm_fields=state")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono<FmsErrorResponse>().flatMap { error ->
            Mono.error(
              SercoConnectionException(
                "Error fetching state for case $caseId: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono<FmsStateResponse>()
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!

    return result
  }
}
